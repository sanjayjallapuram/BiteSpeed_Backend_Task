package com.bitespeed.identity_reconciliation.dto;

import lombok.Data;

@Data
public class IdentifyResponse {
    private ContactResponse contact;

    public IdentifyResponse(ContactResponse contact){
        this.contact=contact;
    }

    public ContactResponse getContact(){
        return contact;
    }

    public void setContact(ContactResponse contact){
        this.contact=contact;
    }
}
