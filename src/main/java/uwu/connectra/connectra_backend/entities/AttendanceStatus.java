package uwu.connectra.connectra_backend.entities;

public enum AttendanceStatus {
    PRESENT,             // >= 80%
    PARTIALLY_PRESENT,   // > 0% but < 80%
    ABSENT               // 0% or didn't join
}
