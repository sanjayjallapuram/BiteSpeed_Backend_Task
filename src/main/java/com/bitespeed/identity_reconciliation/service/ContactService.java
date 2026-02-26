package com.bitespeed.identity_reconciliation.service;

import com.bitespeed.identity_reconciliation.dto.ContactResponse;
import com.bitespeed.identity_reconciliation.dto.IdentifyRequest;
import com.bitespeed.identity_reconciliation.dto.IdentifyResponse;
import com.bitespeed.identity_reconciliation.entity.Contact;
import com.bitespeed.identity_reconciliation.entity.LinkPrecedence;
import com.bitespeed.identity_reconciliation.repository.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContactService {

    private final ContactRepository repository;

    public ContactService(ContactRepository repository){
        this.repository=repository;
    }

    @Transactional
    public IdentifyResponse identify(IdentifyRequest request) {

        String email = request.getEmail();
        String phone = request.getPhoneNumber();

        List<Contact> matched = repository.findByEmailOrPhoneNumber(email, phone);

        // Case 1: No existing contact
        if (matched.isEmpty()) {
            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phone);
            newContact.setLinkPrecedence(LinkPrecedence.PRIMARY);
            repository.save(newContact);

            return buildResponse(newContact.getId());
        }

        // Collect all related contacts
        Set<Contact> allContacts = new HashSet<>(matched);

        for (Contact c : matched) {
            if (c.getLinkedId() != null) {
                allContacts.addAll(repository.findByLinkedId(c.getLinkedId()));
                repository.findById(c.getLinkedId()).ifPresent(allContacts::add);
            } else {
                allContacts.addAll(repository.findByLinkedId(c.getId()));
            }
        }

        // Find oldest primary
        Contact primary = allContacts.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();

        // Convert other primaries into secondary
        for (Contact c : allContacts) {
            if (!c.getId().equals(primary.getId())
                    && c.getLinkPrecedence() == LinkPrecedence.PRIMARY) {

                c.setLinkPrecedence(LinkPrecedence.SECONDARY);
                c.setLinkedId(primary.getId());
                repository.save(c);
            }
        }

        // Create secondary if new info
        boolean emailExists = allContacts.stream()
                .anyMatch(c -> email != null && email.equals(c.getEmail()));

        boolean phoneExists = allContacts.stream()
                .anyMatch(c -> phone != null && phone.equals(c.getPhoneNumber()));

        if ((!emailExists && email != null) || (!phoneExists && phone != null)) {
            Contact secondary = new Contact();
            secondary.setEmail(email);
            secondary.setPhoneNumber(phone);
            secondary.setLinkPrecedence(LinkPrecedence.SECONDARY);
            secondary.setLinkedId(primary.getId());
            repository.save(secondary);
            allContacts.add(secondary);
        }

        return buildResponse(primary.getId());
    }

    private IdentifyResponse buildResponse(Long primaryId) {

        Contact primary = repository.findById(primaryId).orElseThrow();
        List<Contact> secondaries = repository.findByLinkedId(primaryId);

        Set<String> emails = new LinkedHashSet<>();
        Set<String> phones = new LinkedHashSet<>();
        List<Long> secondaryIds = new ArrayList<>();

        if (primary.getEmail() != null)
            emails.add(primary.getEmail());

        if (primary.getPhoneNumber() != null)
            phones.add(primary.getPhoneNumber());

        for (Contact c : secondaries) {
            if (c.getEmail() != null)
                emails.add(c.getEmail());

            if (c.getPhoneNumber() != null)
                phones.add(c.getPhoneNumber());

            secondaryIds.add(c.getId());
        }

        ContactResponse response = new ContactResponse();
        response.setPrimaryContactId(primaryId);
        response.setEmails(new ArrayList<>(emails));
        response.setPhoneNumbers(new ArrayList<>(phones));
        response.setSecondaryContactIds(secondaryIds);

        return new IdentifyResponse(response);
    }
}
