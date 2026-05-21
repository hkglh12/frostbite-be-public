classDiagram
direction BT
class node2 {
   bigint file_id  /* 접근제어 파일 식별자 */
   integer effect_type  /* 접근제어 종류 */
   integer subject_type  /* 피접근제어 종류 */
   integer subject_id  /* 피 접근제어 식별자 */
   integer permission  /* 제약 권한 */
   smallint is_shared  /* 공유로 생성된 제약 여부 */
   timestamp create_time  /* 접근제어 생성시각 */
   integer create_user  /* 제약 생성자 */
   smallint is_delete  /* 삭제여부 */
   bigint acl_id  /* 접근제어 식별자 */
}
class node6 {
   integer upload_status  /* 압로드 상태 */
   bigint file_id  /* 파일 식별자 */
}
class node3 {
   varchar(1024) file_name  /* 파일명 */
   bigint file_size  /* 파일크기 */
   bigint folder_id  /* 부모 폴더 식별자 */
   varchar(512) mount_point  /* 서버상 파일 최상위경로 */
   varchar(1024) file_path  /* 서버상 파일경로 */
   integer create_user  /* 셍성자 */
   timestamp create_time  /* 생성시각 */
   integer modify_user  /* 수정자 */
   timestamp modify_time  /* 수정시각 */
   uuid uuid  /* UUID */
   smallint is_delete  /* 삭제여부 */
   bigint file_id  /* 파일 식별자 */
}
class node9 {
   bigint file_id
   varchar(1024) file_name  /* 파일명 */
   bigint file_size  /* 파일크기 */
   varchar(256) operation_description
   integer modify_user  /* 수정자 */
   timestamp modify_time  /* 수정시각 */
   bigint file_ver_id  /* 파일 변경이력 식별자 */
}
class node8 {
   bigint folder_id  /* 접근제어 폴더 식별자 */
   integer effect_type  /* 접근제어 종류 */
   subject_type  /* 피접근제어 종류 (1=USER, 2=TEAM) */ integer
   subject_id  /* 피접근제어 식별자 (subject_type=USER이면 user_tb.user_id, TEAM이면 team_t... */ integer
   integer permission  /* 제약 권한 */
   smallint is_shared  /* 공유로 생성된 제약 여부 */
   timestamp create_time  /* 접근제어 생성시각 */
   integer create_user  /* 제약 생성자 */
   smallint is_delete  /* 삭제여부 */
   bigint acl_id  /* 접근제어 식별자 */
}
class node0 {
   varchar(512) name  /* 폴더명 */
   varchar(256) owner_type  /* 종속종류 */
   integer resource_scope_id  /* 피종속 자원 식별자 */
   integer create_user  /* 폴더 생성자 */
   timestamp create_time  /* 생성시각 */
   integer modify_user
   timestamp modify_time
   integer folder_type  /* 폴더종류 */
   bigint parent_id  /* 부모 폴더 식별자 */
   integer render_sequence  /* 출력순서 */
   uuid uuid  /* uuid */
   smallint is_delete  /* 삭제여부 */
   bigint folder_id  /* 폴더 식별자 */
}
class node1 {
   integer folder_id  /* 폴더 식별자 */
   varchar(512) name  /* 폴더명 */
   integer modify_user  /* 수정자 */
   timestamp modify_time  /* 수정시각 */
   bigint folder_ver_id  /* 폴더 버전 식별자 */
}
class node5 {
   integer user_id  /* 사용자 식별자 */
   integer role_id  /* 역할 식별자 */
   integer rel_id  /* 관계 식별자 */
}
class node10 {
   integer user_id  /* 사용자 식별자 */
   integer team_id  /* 팀 식별자 */
   integer create_user  /* 초대자 */
   timestamp join_time  /* 합류시각 */
   smallint is_delete  /* 삭제여부 */
   integer rel_id  /* 사용자-팀 관계식별자 */
}
class node7 {
   varchar(256) role_name  /* 역할명 */
   integer role_id  /* 역할 식별자 */
}
class node4 {
   varchar(256) name  /* 팀 이름 */
   timestamp create_time  /* 생성시각 */
   integer create_user  /* 생성자 */
   uuid uuid  /* 팀 UUID */
   smallint is_delete  /* 삭제여부 */
   integer team_id  /* 팀 식별자 */
}
class node11 {
   varchar(128) name  /* 사용자이름 */
   varchar(256) email  /* 이메일 */
   varchar(1024) password  /* 비밀번호 */
   varchar(14) phone_number  /* 휴대전화번호 */
   date birth  /* 생년월일 */
   date create_date  /* 가입날짜 */
   uuid uuid  /* 사용자 UUID */
   smallint is_delete  /* 삭제여부 */
   integer user_id  /* 사용자 식별자 */
}

node2  -->  node3 : file_id
node2  -->  node11 : create_user:user_id
node6  -->  node3 : file_id
node3  -->  node0 : folder_id
node3  -->  node11 : create_user:user_id
node3  -->  node11 : modify_user:user_id
node9  -->  node3 : file_id
node9  -->  node11 : modify_user:user_id
node8  -->  node0 : folder_id
node8  -->  node11 : create_user:user_id
node8  -->  node11 : subject_id:user_id
node0  -->  node0 : parent_id:folder_id
node0  -->  node11 : modify_user:user_id
node0  -->  node11 : create_user:user_id
node1  -->  node0 : folder_id
node1  -->  node11 : modify_user:user_id
node5  -->  node7 : role_id
node5  -->  node11 : user_id
node10  -->  node4 : team_id
node10  -->  node11 : user_id
node4  -->  node11 : create_user:user_id
