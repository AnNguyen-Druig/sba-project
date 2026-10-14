package com.sba.project.modules.maintenance.dto.request;

/** Optional note stored on the STATUS record written by an accept/reject/start/complete action. */
public record TransitionRequest(String note) {
}
