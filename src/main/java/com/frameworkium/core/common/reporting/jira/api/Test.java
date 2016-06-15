package com.frameworkium.core.common.reporting.jira.api;

import com.frameworkium.core.common.reporting.jira.Config;
import io.restassured.path.json.JsonPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.*;

public class Test {

    private final static Logger logger = LogManager.getLogger(Test.class);

    public static void changeIssueFieldValue(
            String issueKey, String fieldToUpdate, String resultValue) {
        JSONObject obj = new JSONObject();
        JSONObject fieldObj = new JSONObject();
        JSONObject setObj = new JSONObject();
        JSONArray setArr = new JSONArray();
        JSONObject valueObj = new JSONObject();

        try {
            obj.put("update", fieldObj);
            fieldObj.put(getFieldId(fieldToUpdate), setArr);
            setArr.put(setObj);
            setObj.put("set", valueObj);
            valueObj.put("value", resultValue);

        } catch (JSONException e) {
            logger.error("Can't create JSON Object for test case result update", e);
        }

        Config.getJIRARequestSpec()
                .contentType("application/json").and()
                .body(obj.toString())
                .when()
                .put(Config.jiraRestURI + "/issue/" + issueKey);
    }

    private static String getFieldId(String fieldName) {

        JsonPath jsonPath = Config.getJIRARequestSpec()
                .when()
                .get(Config.jiraRestURI + "/field")
                .thenReturn().jsonPath();

        return jsonPath.getString(String.format("find {it.name == '%s'}.id", fieldName));
    }

    public static void addComment(String issueKey, String commentToAdd) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("body", commentToAdd);
        } catch (JSONException e) {
            logger.error("Can't create JSON Object for comment update", e);
        }

        Config.getJIRARequestSpec()
                .contentType("application/json")
                .body(obj.toString())
                .when()
                .post(Config.jiraRestURI + "/issue/" + issueKey + "/comment");
    }

    private static int getTransitionId(String issueKey, String transitionName) {

        JsonPath jsonPath = Config.getJIRARequestSpec()
                .when()
                .get(Config.jiraRestURI + "/issue/" + issueKey + "?expand=transitions.fields")
                .thenReturn().jsonPath();

        String jsonPathToTransitionId = String.format(
                "transitions.find {it -> it.name == '%s'}.id", transitionName);
        return Integer.parseInt(jsonPath.getString(jsonPathToTransitionId));
    }

    public static void transitionIssue(String issueKey, String transitionName) {
        logger.debug("Transition name: " + transitionName);
        transitionIssue(issueKey, getTransitionId(issueKey, transitionName));
    }

    private static void transitionIssue(String issueKey, int transitionId) {
        logger.debug("Transition id: " + transitionId);
        JSONObject obj = new JSONObject();
        JSONObject idObj = new JSONObject();

        try {
            obj.put("transition", idObj);
            idObj.put("id", transitionId);
        } catch (JSONException e) {
            logger.error("Can't create JSON Object for transition change", e);
        }

        Config.getJIRARequestSpec()
                .contentType("application/json").and()
                .body(obj.toString())
                .when()
                .post(Config.jiraRestURI + "issue/" + issueKey + "/transitions");
    }

}
