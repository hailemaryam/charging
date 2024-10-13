package com.hmmk.melkite.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "SendPayItem")
@Cacheable
public class SendPayItem {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    public String spId;
    public String hash;
    public String serviceId;
    public String phone;
}
