package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Contact
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContactRepo : CoroutineCrudRepository<Contact, Int>