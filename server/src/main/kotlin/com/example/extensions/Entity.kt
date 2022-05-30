package com.example.extensions

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Op

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.findOne(op: Op<Boolean>): T? =
    this.find(op).limit(1).firstOrNull()