package io;

import model.Candidate;
import model.Edge;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV reader for this project.
 *
 * <p>Deliberately narrow in scope: the provided datasets have a header row,
 * plain comma separators, no quoted fields, and no escaping. A full RFC 4180
 * parser would be overkill and would also pull us outside the CPT204 library
 * allowlist.
 */
public final class CsvReader {

    private CsvReader() {
        // utility
    }

    /**
     * Reads a candidates file of the form {@code location_id,priority_score}.
     * The first line is treated as a header and skipped.
     */
    public static List<Candidate> readCandidates(Path file) throws IOException {
        List<Candidate> result = new ArrayList<>(1024);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("empty file: " + file);
            }
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length != 2) {
                    throw new IOException("malformed candidate row at "
                            + file + ":" + lineNo + " -> " + line);
                }
                String id = parts[0].trim();
                int score;
                try {
                    score = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException nfe) {
                    throw new IOException("bad priority_score at "
                            + file + ":" + lineNo + " -> " + parts[1], nfe);
                }
                result.add(new Candidate(id, score));
            }
        }
        return result;
    }

    /**
     * Reads a paths file of the form {@code from_location,to_location,weight}.
     * The first line is treated as a header and skipped.
     */
    public static List<Edge> readEdges(Path file) throws IOException {
        List<Edge> result = new ArrayList<>(4096);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("empty file: " + file);
            }
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length != 3) {
                    throw new IOException("malformed edge row at "
                            + file + ":" + lineNo + " -> " + line);
                }
                String from = parts[0].trim();
                String to = parts[1].trim();
                double weight;
                try {
                    weight = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException nfe) {
                    throw new IOException("bad weight at "
                            + file + ":" + lineNo + " -> " + parts[2], nfe);
                }
                result.add(new Edge(from, to, weight));
            }
        }
        return result;
    }
}
