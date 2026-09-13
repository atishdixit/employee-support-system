package com.ext.emp.support.model;

import java.time.LocalDate;

/** A seeded, in-memory employee record — no database, per the project's scope. */
public record Employee(
        String id,
        String name,
        String department,
        String designation,
        LocalDate joiningDate) {
}
