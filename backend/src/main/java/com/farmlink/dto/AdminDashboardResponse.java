package com.farmlink.dto;

import java.util.List;

/** Payload for the admin dashboard: live counts from the database plus the newest accounts. */
public record AdminDashboardResponse(
        long totalUsers,
        long farmers,
        long buyers,
        long admins,
        List<UserResponse> recentUsers
) {
}
