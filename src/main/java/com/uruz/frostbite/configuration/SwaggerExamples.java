package com.uruz.frostbite.configuration;

public class SwaggerExamples {
    private SwaggerExamples() {}

    /** FOLDER CONTROLLER */

    public static final String SIMPLE_FOLDER_RESPONSE_SUCCESS = """
    {
      "result": {
        "uuid" : "0f7e2d62-2f45-4c47-9b3f-9e62fb041f31",
        "name" : "프로젝트 폴더",
        "folderType" : "PRIVATE",
        "renderSequence" : 1,
        "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
        "status" : "ACTIVE"
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String SIMPLE_FOLDER_LIST_OFFSET_RESPONSE_SUCCESS = """
    {
      "result": {
        "folders" : [
          {
            "uuid" : "0f7e2d62-2f45-4c47-9b3f-9e62fb041f31",
            "name" : "project-docs",
            "folderType" : "PRIVATE",
            "renderSequence" : 1,
            "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
            "status" : "ACTIVE"
          },
          {
            "uuid" : "2be0e687-2c5f-42d1-8fd7-5e6f3a84ecb2",
            "name" : "meeting-notes",
            "folderType" : "PRIVATE",
            "renderSequence" : 2,
            "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
            "status" : "ACTIVE"
          }
        ],
        "total" : 2,
        "currentMax" : 2
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String SIMPLE_FOLDER_LIST_KEYSET_RESPONSE_SUCCESS = """
    {
      "result": {
        "folders" : [
          {
            "uuid" : "0f7e2d62-2f45-4c47-9b3f-9e62fb041f31",
            "name" : "project-docs",
            "folderType" : "PRIVATE",
            "renderSequence" : 1,
            "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
            "status" : "ACTIVE"
          },
          {
            "uuid" : "2be0e687-2c5f-42d1-8fd7-5e6f3a84ecb2",
            "name" : "meeting-notes",
            "folderType" : "PRIVATE",
            "renderSequence" : 2,
            "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
            "status" : "ACTIVE"
          }
        ],
        "hasNext" : true,
        "lastFolderUuid" : "2be0e687-2c5f-42d1-8fd7-5e6f3a84ecb2"
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String SIMPLE_FOLDER_ENTRIES_LIST_OFFSET_RESPONSE_SUCCESS = """
    {
      "result": {
        "entries" : [
          {
            "uuid" : "0f7e2d62-2f45-4c47-9b3f-9e62fb041f31",
            "name" : "project-docs",
            "entryType" : "1",
            "renderSequence" : 1,
            "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
            "status" : "ACTIVE"
          },
          {
            "uuid" : "2be0e687-2c5f-42d1-8fd7-5e6f3a84ecb2",
            "name" : "meeting-notes",
            "folderType" : "PRIVATE",
            "renderSequence" : 2,
            "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
            "status" : "ACTIVE"
          }
        ],
        "entryTotal" : 2,
        "folderTotal" : 1,
        "fileTotal" : 1
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String FOLDER_DETAIL_RESPONSE_SUCCESS = """
    {
      "result": {
        "uuid" : "0f7e2d62-2f45-4c47-9b3f-9e62fb041f31",
        "name" : "project-docs",
        "folderType" : "PRIVATE",
        "ownerName" : "hong-gildong",
        "createUserName" : "hong-gildong",
        "createTime" : "2026-04-18",
        "modifyUserName" : "hong-gildong",
        "modifyTime" : "2026-04-18",
        "renderSequence" : 1,
        "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
        "status" : "ACTIVE"
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String FOLDER_MODIFY_RESPONSE_SUCCESS = """
    {
      "result": {
        "uuid" : "0f7e2d62-2f45-4c47-9b3f-9e62fb041f31",
        "name" : "renamed-project-docs",
        "folderType" : "PRIVATE",
        "renderSequence" : 1,
        "parentFolderUuid" : "7e6c8c12-119f-4a12-8eb7-c87506f1eaa7",
        "status" : "ACTIVE"
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String FOLDER_MOVE_RESPONSE_SUCCESS = """
    {
      "result": {
        "uuid" : "0f7e2d62-2f45-4c47-9b3f-9e62fb041f31",
        "name" : "project-docs",
        "folderType" : "PRIVATE",
        "renderSequence" : 3,
        "parentFolderUuid" : "92df72b0-3458-4a2c-a62c-4dfcb5711429",
        "status" : "ACTIVE"
      },
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String FOLDER_REMOVE_RESPONSE_SUCCESS = """
    {
      "result": true,
      "message": "SUCCEED",
      "code" : "SUCCEED",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;

    public static final String FOLDER_TARGET_NOT_FOUND = """
{
  "message": "FOLDER NOT FOUND",
  "code": "FOLDER NOT FOUND",
  "codeMessage": "unabled to find request target",
  "timestamp": "2026-05-01 19:01:01"
}
""";

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
