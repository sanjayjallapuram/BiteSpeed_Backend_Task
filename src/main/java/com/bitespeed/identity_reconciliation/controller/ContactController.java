package com.bitespeed.identity_reconciliation.controller;

import com.bitespeed.identity_reconciliation.dto.IdentifyRequest;
import com.bitespeed.identity_reconciliation.dto.IdentifyResponse;
import com.bitespeed.identity_reconciliation.entity.Contact;
import com.bitespeed.identity_reconciliation.repository.ContactRepository;
import com.bitespeed.identity_reconciliation.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/identify")
public class ContactController {
    private final ContactService service;

    @Autowired
    private ContactRepository contactRepository;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping
    public IdentifyResponse identify(@RequestBody IdentifyRequest request) {
        return service.identify(request);
    }

    @GetMapping()
    public List<Contact> getAll() {
        return contactRepository.findAll();
    }

}