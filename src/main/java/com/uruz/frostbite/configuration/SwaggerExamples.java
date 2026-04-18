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
            {
              "uuid": "a62d14fb-74a3-4d8b-99fc-ce0248c9438c",
              "name": "홍길동",
              "email": "confi@frostbite.com",
              "phoneNumber": "01041211101",
              "birth": "1994-01-01",
              "createDate": "2026-04-18"
            }
    """;

    public static final String USER_SELF_GET_FAILED = """
    {
      "message": "USER NOF FOUND",
      "code" : "NOT_FOUND",
      "codeMessage" : "Unable to find request target",
      "timestamp" : "2026-04-18 19:01:01"
    }
    """;
}
