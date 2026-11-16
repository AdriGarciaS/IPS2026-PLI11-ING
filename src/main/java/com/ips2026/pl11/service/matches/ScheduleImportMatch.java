package com.ips2026.pl11.service.matches;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ips2026.pl11.model.matches.MatchCSV;

public class ScheduleImportMatch {

	public static class ImportResult {
        private final List<MatchCSV> validMatches = new ArrayList<>();
        private final List<String> errorLogs = new ArrayList<>();
        private final List<String> warningLogs = new ArrayList<>();

        public List<MatchCSV> getValidMatches() { 
        	return validMatches; 
        }
        
        public List<String> getErrorLogs() { 
        	return errorLogs; 
        }
        
        public List<String> getWarningLogs() { 
        	return warningLogs; 
        }
    }

    public ImportResult processFile(File file, int defaultTeamId, int defaultFacilityId) {
        ImportResult result = new ImportResult();
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".csv")) {
            parseCsv(file, defaultTeamId, defaultFacilityId, result);
        } else {
            result.getErrorLogs().add("[ERROR] Unsupported file format. Please use a CSV schedule file.");
        }

        return result;
    }

    private void parseCsv(File file, int defaultTeamId, int defaultFacilityId, ImportResult result) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            int rowNum = 0;

            while ((line = br.readLine()) != null) {
                rowNum++;
                if (header) {
                    header = false; 
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    result.getErrorLogs().add("[ERROR] Row " + rowNum + ": Skipped due to missing required fields.");
                    continue;
                }

                try {
                    String matchDate = parts[0].trim();
                    String rival = parts[1].trim();
                    LocalTime startTime = LocalTime.parse(parts[2].trim());
                    LocalTime endTime = LocalTime.parse(parts[3].trim());
                    boolean isHome = Boolean.parseBoolean(parts[4].trim());
                    boolean isSpecialMatch = parts.length > 5 && Boolean.parseBoolean(parts[5].trim());

                    if (matchDate.isEmpty() || rival.isEmpty()) {
                        result.getErrorLogs().add("[ERROR] Row " + rowNum + ": Missing date or rival.");
                        continue;
                    }

                    MatchCSV matchDTO = new MatchCSV(defaultTeamId, matchDate, startTime, endTime, rival, isHome, isSpecialMatch, defaultFacilityId);
                    result.getValidMatches().add(matchDTO);

                } catch (Exception ex) {
                    result.getErrorLogs().add("[ERROR] Row " + rowNum + ": Format error (" + ex.getMessage() + ")");
                }
            }
        } catch (Exception e) {
            result.getErrorLogs().add("[ERROR] Failed to read CSV file: " + e.getMessage());
        }
    }
	
}
