package com.sliit.vrs.entity;

// One role per login. Used for simple Role-Based Access Control (RBAC).
// This satisfies the "User Registration & Authentication" minor function
// that is shared by every module.
public enum Role {
    CUSTOMER,
    ADMIN,                 // System Administrator
    FLEET_MANAGER,         // Member 1 - Vehicle Fleet Management
    OPERATIONS_SUPERVISOR, // Member 4 - Handover/Return/Damage
    MAINTENANCE_STAFF,     // Member 6 - Maintenance & Driver Allocation
    DRIVER                 // Senior Driver
}
