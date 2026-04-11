### N+1 처리

- 기존 현업 코드 작성 시 부모 읽어오고 자손 Collection 조회하기위해 +1 코드 작성하던것을(nested select) -> JOIN + nested Results 방식으로 전환
- resultType 대신 resultMap을 select 태그에 선언, resultMap id에 해당 값 사용, Type에 결과 객체 매핑
```xml
<select id="selectBlog" resultMap="blogResult">
    SELECT
        b.id   AS blog_id,
        b.title AS blog_title,
        p.id   AS post_id,
        p.subject AS post_subject,
        p.body AS post_body
    FROM blog b
    LEFT OUTER JOIN post p ON b.id = p.blog_id
    WHERE b.id = #{id}
</select>

<resultMap id="blogResult" type="Blog">
    <id property="id" column="blog_id"/>
    <result property="title" column="blog_title"/>
    <collection property="posts" ofType="Post">
        <id property="id" column="post_id"/>
        <result property="subject" column="post_subject"/>
        <result property="body" column="post_body"/>
    </collection>
</resultMap>
```

