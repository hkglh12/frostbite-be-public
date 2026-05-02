package com.uruz.frostbite.configuration;

public class SwaggerExamples {
    private SwaggerExamples() {}

    /** USERCONTROLLER */

    public static final String USER_JOIN_SUCCESS = """
    {
      "result": {
        "name" : "홍길동",
        "email" : "confi@frostbite.com",
        "phoneNumber" : "01041211101",
        "birth" : "1994-01-01",
        "createDate" : "2026-04-18",
        "uuid" : "a62d14fb-74a3-4d8b-99fc-ce0248c9438c",
        "status" : "ACTIVE"
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String USER_JOIN_BAD_REQUEST = """
    {
      "message": "IllegalParameterException",
      "code" : "UNSATISFIED",
      "codeMessage" : "Required parameter not satisfied",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String USER_SELF_GET_SUCCEESS = """
        "result" : {
              "uuid": "a62d14fb-74a3-4d8b-99fc-ce0248c9438c",
              "name": "홍길동",
              "email": "confi@frostbite.com",
              "phoneNumber": "01041211101",
              "birth": "1994-01-01",
              "createDate": "2026-04-18"
            },
        "message": "SUCCEED",
        "code" : "SUCCEED",
        "timestamp" : "2026-04-18 19:01:01"
    """;

    public static final String USER_SELF_GET_FAILED = """
    {
      "message": "USER NOF FOUND",
      "code" : "NOT_FOUND",
      "codeMessage" : "Unable to find request target",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String USER_LOGIN_PARAMETER_EXAMPLE = """
            {
                "email": "test@test.com",
                "password": "1234"
                }
            """;


    public static final String USER_LOGIN_SUCCESS_EXAMPLE = """
            {
                "email": "test@test.com",
                "password": "1234",
                "message": "SUCCEED",
                "code" : "SUCCEED",
                "timestamp" : "2026-04-18 19:01:01"
                }
            """;

    public static final String USER_LOGIN_FAILURE_EXAMPLE = """
    {
      "message": "AUTHENTICATION_FAILED",
      "code" : "AUTHENTICATION_FAILED",
      "codeMessage" : "Authentication failed",
      "timestamp" : "2026-04-18 19:01:01"
    }
            """;


    /** TEAM CONTROLLER */

    public static final String TEAM_CREATE_SUCCESS_EXAMPLE = """
{
  "result": {
    "name": "개발팀",
    "createTime": "2026-05-01",
    "createUserName": "이준훈",
    "modifyTime": null,
    "modifyUserName": null,
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "status": "ACTIVE"
  },
  "message": "SUCCEED",
  "code": "SUCCEED",
  "timestamp": "2026-05-01 19:01:01"
}
""";
    public static final String TEAM_CREATE_BAD_REQUEST_EXAMPLE = """
{
  "message": "VALIDATION_FAILED",
  "code": "VALIDATION_FAILED",
  "codeMessage": "Team name must not be empty",
  "timestamp": "2026-05-01 19:01:01"
}
""";
    public static final String TEAM_LIST_SUCCESS_EXAMPLE = """
{
  "result": [
    {
      "name": "개발팀",
      "createTime": "2026-05-01",
      "createUserName": "이준훈",
      "modifyTime": null,
      "modifyUserName": null,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "status": "ACTIVE"
    },
    {
      "name": "기획팀",
      "createTime": "2026-05-01",
      "createUserName": "홍길동",
      "modifyTime": "2026-05-01",
      "modifyUserName": "홍길동",
      "uuid": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "status": "ACTIVE"
    }
  ],
  "message": "SUCCEED",
  "code": "SUCCEED",
  "timestamp": "2026-05-01 19:01:01"
}
""";

    public static final String TEAM_LIST_SUCCESS_EMPTY_EXAMPLE = """
{
  "result": [
    {
      "name": "개발팀",
      "createTime": "2026-05-01",
      "createUserName": "이준훈",
      "modifyTime": null,
      "modifyUserName": null,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "status": "ACTIVE"
    },
    {
      "name": "기획팀",
      "createTime": "2026-05-01",
      "createUserName": "홍길동",
      "modifyTime": "2026-05-01",
      "modifyUserName": "홍길동",
      "uuid": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "status": "ACTIVE"
    }
  ],
  "message": "SUCCEED",
  "code": "SUCCEED",
  "timestamp": "2026-05-01 19:01:01"
}
""";

    public static final String TEAM_DETAIL_SUCCESS_EXAMPLE = """
{
  "result": {
    "name": "개발팀",
    "createTime": "2026-05-01",
    "createUserName": "이준훈",
    "modifyTime": "2026-05-01",
    "modifyUserName": "이준훈",
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "status": "ACTIVE"
  },
  "message": "SUCCEED",
  "code": "SUCCEED",
  "timestamp": "2026-05-01 19:01:01"
}
""";

    public static final String TEAM_DETAIL_NOT_FOUND_EXAMPLE = """
{
  "message": "TEAM_NOT_FOUND",
  "code": "TEAM_NOT_FOUND",
  "codeMessage": "Team not found",
  "timestamp": "2026-05-01 19:01:01"
}
""";


    public static final String TEAM_MODIFY_SUCCESS_EXAMPLE = """
{
  "result": {
    "name": "백엔드 개발팀",
    "createTime": "2026-05-01",
    "createUserName": "이준훈",
    "modifyTime": "2026-05-01",
    "modifyUserName": "이준훈",
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "status": "ACTIVE"
  },
  "message": "SUCCEED",
  "code": "SUCCEED",
  "timestamp": "2026-05-01 19:01:01"
}
""";

    public static final String TEAM_MODIFY_BAD_REQUEST_EXAMPLE = """
{
  "message": "VALIDATION_FAILED",
  "code": "VALIDATION_FAILED",
  "codeMessage": "Team uuid and name must not be null",
  "timestamp": "2026-05-01 19:01:01"
}
""";
    public static final String TEAM_REMOVE_SUCCESS_EXAMPLE = """
{
  "result": null,
  "message": "SUCCEED",
  "code": "SUCCEED",
  "timestamp": "2026-05-01 19:01:01"
}
""";

    public static final String TEAM_REMOVE_NOT_FOUND_EXAMPLE = """
{
  "message": "TEAM_NOT_FOUND",
  "code": "TEAM_NOT_FOUND",
  "codeMessage": "Team not found",
  "timestamp": "2026-05-01 19:01:01"
}
""";

}
