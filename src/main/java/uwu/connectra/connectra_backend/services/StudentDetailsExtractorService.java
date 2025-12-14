package uwu.connectra.connectra_backend.services;

import org.springframework.stereotype.Service;

@Service
public class StudentDetailsExtractorService {

    private static final String EMAIL_SUFFIX = "@std.uwu.ac.lk";
    private static final int INDEX_LENGTH = 3;
    private static final int BATCH_LENGTH = 2;
    private static final int NUMERIC_SUFFIX_LENGTH = INDEX_LENGTH + BATCH_LENGTH;

    // === GET DEGREE ===
    public String extractDegree(String studentEmail) {
        validateEmail(studentEmail);
        String userPart = getUserPart(studentEmail);

        //everything before the batch and index
        String degree = userPart.substring(0, userPart.length() - NUMERIC_SUFFIX_LENGTH);
        return degree.toUpperCase();

    }
    // === GET STUDENT ID ===
    public String extractStudentId(String studentEmail) {
        validateEmail(studentEmail);
        String userPart = getUserPart(studentEmail);

        String degree = extractDegree(studentEmail);
        String batch = userPart.substring(userPart.length() - NUMERIC_SUFFIX_LENGTH, userPart.length() - INDEX_LENGTH);
        String index = userPart.substring(userPart.length() - INDEX_LENGTH);

        //UWU/{DEGREE}/{BATCH}/{INDEX}
        return String.format("UWU/%s/%s/%s", degree, batch, index);
    }

    // === GET BATCH YEAR ===
    public int extractBatch(String studentEmail) {
        validateEmail(studentEmail);
        String userPart = getUserPart(studentEmail);

        //  before the Index
        int startIndex = userPart.length() - NUMERIC_SUFFIX_LENGTH;
        int endIndex = userPart.length() - INDEX_LENGTH;

        String batchStr = userPart.substring(startIndex, endIndex);

        try {
            return Integer.parseInt(batchStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse batch year from email: " + studentEmail);
        }
    }
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if (!email.endsWith(EMAIL_SUFFIX)) {
            throw new IllegalArgumentException("Invalid email domain. Must end with " + EMAIL_SUFFIX);
        }

        // user part is long enough to contain degree (at least 1 char) + batch (2) + index (3)
        String userPart = getUserPart(email);
        if (userPart.length() <= NUMERIC_SUFFIX_LENGTH) {
            throw new IllegalArgumentException("Email format invalid: User part too short to contain degree, batch, and index.");
        }
    }
    private String getUserPart(String email) {
        int atIndex = email.indexOf("@");
        // Validation ensure @ exists because we checked endsWith suffix
        return email.substring(0, atIndex);
    }
}
