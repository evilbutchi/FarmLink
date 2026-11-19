package com.farmlink.dto;

/** Payload for the farmer and buyer dashboards. */
public record ProfileDashboardResponse(String welcome, UserResponse profile) {
}
