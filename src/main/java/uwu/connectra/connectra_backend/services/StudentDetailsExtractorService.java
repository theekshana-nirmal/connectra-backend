package uwu.connectra.connectra_backend.services;

import org.springframework.stereotype.Component;

public class StudentDetailsExtractorService {
    // === GET STUDENT ID ===
    public String extractStudentId(String studentEmail) {
        // TODO: Implement the logic to extract student ID from the email

        // INPUT: Student email in the format like "ict22082@std.uwu.ac.lk"
        // OUTPUT: "UWU/ICT/22/082"
        // Rule: "UWU/{DEGREE}/{BATCH}/{INDEX}"

        return null;
    }

    // === GET DEGREE ===
    public String extractDegree(String studentEmail) {
        // TODO: Implement the logic to extract degree code from the email

        // INPUT: Student email in the format like "bbst21004@std.uwu.ac.lk"
        // OUTPUT: "BBST"
        // Rule: Before the last five digits, the preceding letters represent the degree code. and output should be in uppercase.

        return null;
    }

    // === GET BATCH YEAR ===
    public int extractBatch(String studentEmail) {
        // TODO: Implement the logic to extract batch year from the email

        // INPUT: Student email in the format like "bet23103@std.uwu.ac.lk"
        // OUTPUT: 23
        // Rule: Before the last three digits, the two digits represent the batch year.

        return 0;
    }
}
