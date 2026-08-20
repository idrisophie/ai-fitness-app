package org.idrisophie.fitness.aiservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.idrisophie.fitness.aiservice.models.Activity;
import org.idrisophie.fitness.aiservice.models.Recommendation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ActivityAIService {
    private final GeminiService geminiService;

    public ActivityAIService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public Recommendation generateRecommendation(Activity activity){
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("RESPONSE FROM AI: {} ", aiResponse);
        return processAiResponse(activity, aiResponse);
    }
    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode textNode = rootNode.path("candidate")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");
            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();
            //    log.info("RESPONSE FROM AI: {} ", jsonContent);

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));

            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));

            List<String> safety = extractSafetyGuideline(analysisJson.path("safety"));
            return Recommendation.builder()
                    .acvtivityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(Arrays.asList(
                            "Always warm up befor exercise",
                            "Stay hydrated",
                            "Listen to your body"
                    ))
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return new createDefaultRecommendation(activity);
        }
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s",area, detail));
            });
        }
        return improvements.isEmpty() ?
                Collections.singletonList("No Specific improvements provided"):
                improvements;
    }

    private List<String> extractSuggestions(JsonNode suggestionNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionNode.isArray()) {
            suggestionNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ?
                Collections.singletonList("No Specific suggestions provided") :
                suggestions;
    }

    private List<String> extractSafetyGuideline(JsonNode safetyNode){
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText());
        }
                return safety.isEmpty() ?
                        Collections.singletonList("No general safety provided") :
                        safety;
    }
    private void addAnalysisSection(StringBuilder fullAnalysis,JsonNode analysisNode,String key,String prefix){
        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity){
        return Recommendation.builder().build();
    }
    private String createPromptForActivity(Activity activity){
        return String.format(""" 
                  Analyze this fitness activity and provide detailed recommendations in the following format
                  {
                      "analysis" : {
                          "overall": "Overall analysis here",
                          "pace": "Pace analysis here",
                          "heartRate": "Heart rate analysis here",
                          "CaloriesBurned": "Calories Burned here"
                      },
                      "improvements": [
                          {
                              "area": "Area name",
                              "recommendation": "Detailed Recommendation"
                          }
                      ],
                      "suggestions" : [
                          {
                              "workout": "Workout name",
                              "description": "Detailed workout description"
                          }
                      ],
                      "safety": [
                          "Safety point 1",
                          "Safety point 2"
                      ]
                  }
                
                  Analyze this activity:
                  Activity Type: %s
                  Duration: %d minutes
                  calories Burned: %d
                  Additional Metrics: %s
                
                  provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines
                  Ensure the response follows the EXACT JSON format shown above.              
                """
        );
    }
}
