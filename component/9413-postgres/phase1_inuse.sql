-- 실 사용시, FK 제약은 모두 해제한다. Application Level에서 정합성/무결성 검증을 수행한다. (DROP CASCADE 등 제어가 복잡해지기 때문)

create EXTENSION IF NOT EXISTS "uuid-ossp";

create table user_tb(
                        user_id serial primary key ,
                        name varchar(128),
                        email varchar(256),
                        password varchar(1024),
                        phone_number varchar(14),
                        birth date,
                        create_date date,
                        uuid uuid default uuid_generate_v4(),
                        is_delete smallint
);

comment on table user_tb is '사용자';
comment on column user_tb.user_id is '사용자 식별자';
comment on column user_tb.name is '사용자이름';
comment on column user_tb.email is '이메일';
comment on column user_tb.password is '비밀번호';
comment on column user_tb.phone_number is '휴대전화번호';
comment on column user_tb.birth is '생년월일';
comment on column user_tb.create_date is '가입날짜';
comment on column user_tb.uuid is '사용자 UUID';
comment on column user_tb.is_delete is '삭제여부';

create table team_tb(
                        team_id serial primary key ,
                        name varchar(256),
                        create_time timestamp,
                        create_user int,
                        uuid uuid default uuid_generate_v4(),
                        is_delete smallint
);

-- alter table team_tb add constraint team_tb_user_fk foreign key (create_user) references user_tb (user_id);

comment on table team_tb is '팀';
comment on column team_tb.team_id is '팀 식별자'
comment on column team_tb.name is '팀 이름';
comment on column team_tb.create_time is '생성시각';
comment on column team_tb.create_user is '생성자';
comment on column team_tb.uuid is '팀 UUID';
comment on column team_tb.is_delete is '삭제여부';

create table rel_user_team(
                              rel_id serial primary key ,
                              user_id int,
                              team_id int,
                              create_user int,
                              join_time timestamp,
                              is_delete smallint
);

-- alter table rel_user_team add constraint rel_user_team_user_fk foreign key (user_id) references user_tb (user_id);
-- alter table rel_user_team add constraint rel_user_team_create_user_fk foreign key (user_id) references user_tb (user_id);
-- alter table rel_user_team add constraint rel_user_team_team_fk foreign key (team_id) references team_tb (team_id);

comment on table rel_user_team is '사용자-팀 관계';
comment on column rel_user_team.rel_id is '사용자-팀 관계식별자';
comment on column rel_user_team.user_id is '사용자 식별자';
comment on column rel_user_team.create_user is '초대자';
comment on column rel_user_team.team_id is '팀 식별자';
comment on column rel_user_team.join_time is '합류시각';
comment on column rel_user_team.is_delete is '삭제여부';

create table role_tb(
                        role_id serial primary key ,
                        role_name varchar(256)
);

comment on table role_tb is '역할';
comment on column role_tb.role_id is '역할 식별자';
comment on column role_tb.role_name is '역할명';

create table rel_user_role_tb(
                                 rel_id serial primary key ,
                                 user_id int,
                                 role_id int
);

-- alter table rel_user_role_tb add constraint rel_user_role_tb_user_fk foreign key (user_id) references user_tb (user_id);
-- alter table rel_user_role_tb add constraint rel_user_role_tb_role_fk foreign key (role_id) references role_tb (role_id);

comment on table rel_user_role_tb is '사용자-역할 관계';
comment on column rel_user_role_tb.rel_id is '관계 식별자';
comment on column rel_user_role_tb.user_id is '사용자 식별자';
comment on column rel_user_role_tb.role_id is '역할 식별자';

-- 폴더의 삭제는 "수정"으로 처리할 예정
create table folder_tb(
                          folder_id bigserial primary key,
                          name varchar(512),
                          owner_type varchar(256), -- team || private (개인폴더일경우 user, 팀일경우 team),
                          owner_id int, -- ownertype이 team이면 tema_id가, private 일경우 user_id로
                          create_user int,
                          create_time timestamp,
                          modify_user int,
                          modify_time timestamp,
                          folder_type int,  -- private || team
                          parent_id bigint,
                          render_sequence int,
                          uuid uuid default uuid_generate_v4(),
                          is_delete smallint,

);
-- folder_type은 ENUM으로 1 -> private, 2-> team 으로 추후 구성
-- 팀은 그 자체로 폴더가됨 -> 1depth 폴더는 사실상 해당 팀폴다 아래의 2depth가 됨
-- alter table folder_tb add constraint folder_tb_create_user_fk foreign key (create_user) references user_tb(user_id);
-- alter table folder_tb add constraint folder_tb_modify_user_fk foreign key (modify_user) references user_tb(user_id);
-- alter table folder_tb add constraint folder_tb_folder_parent_fk foreign key (parent_id) references folder_tb(folder_id);

comment on table folder_tb is '폴더';
comment on column folder_tb.folder_id is '폴더 식별자';
comment on column folder_tb.name is '폴더명';
comment on column folder_tb.owner_type is '종속종류';
comment on column folder_tb.owner_id is '피종속 자원 식별자';
comment on column folder_tb.create_user is '폴더 생성자';
comment on column folder_tb.create_time is '생성시각';
comment on column folder_tb.folder_type is '폴더종류'
comment on column folder_tb.parent_id is '부모 폴더 식별자';
comment on column folder_tb.render_sequence is '출력순서';
comment on column folder_tb.uuid is 'uuid';
comment on column folder_tb.is_delete is '삭제여부';

create table folder_tb_ver(
                              folder_ver_id bigserial primary key,
                              folder_id int,
                              name varchar(512),
                              modify_user int,
                              modify_time timestamp
);

-- alter table folder_tb_ver add constraint folder_tb_ver_modify_user_fk foreign key (modify_user) references user_tb(user_id);
-- alter table folder_tb_ver add constraint folder_tb_ver_folder_fk foreign key (folder_id) references folder_tb(folder_id);

-- 폴더명 말고는 바꾸는걸 허용하지 않을예정임
comment on table folder_tb_ver is '폴더 변경이력';
comment on column folder_tb_ver.folder_ver_id is '폴더 버전 식별자';
comment on column folder_tb_ver.folder_id is '폴더 식별자';
comment on column folder_tb_ver.name is '폴더명';
comment on column folder_tb_ver.modify_user is '수정자';
comment on column folder_tb_ver.modify_time is '수정시각';

-- 파일의 삭제는 "수정"으로 처리할 예정
create table file_tb(
                        file_id bigserial primary key ,
                        file_name varchar(1024),
                        file_size bigint,
                        folder_id bigint,
                        mount_point varchar(512),
                        file_path varchar(1024),
                        create_user int,
                        create_time timestamp,
                        modify_user int,
                        modify_time timestamp,

                        uuid uuid default uuid_generate_v4(),
                        is_delete smallint
);

comment on table file_tb is '파일';
comment on column file_tb.file_id is '파일 식별자';
comment on column file_tb.file_name is '파일명';
comment on column file_tb.file_size is '파일크기';
comment on column file_tb.folder_id is '부모 폴더 식별자';
comment on column file_tb.mount_point is '서버상 파일 최상위경로';
comment on column file_tb.file_path is '서버상 파일경로';
comment on column file_tb.create_user is '셍성자';
comment on column file_tb.create_time is '생성시각';
comment on column file_tb.modify_user is '수정자';
comment on column file_tb.modify_time is '수정시각';
comment on column file_tb.uuid is 'UUID';
comment on column file_tb.is_delete is '삭제여부';


-- alter table file_tb add constraint file_tb_folder_fk  foreign key (folder_id) references folder_tb(folder_id);
-- alter table file_tb add constraint file_tb_create_user_fk foreign key (create_user) references user_tb(user_id);
-- alter table file_tb add constraint file_tb_modify_user_fk foreign key (modify_user) references user_tb(user_id);

-- 사실 한개 폴더 내 동일한 파일명일 때 버전관리가 가능할것임. "폴더 단위로 이동이 발생"은 관리하지 않을예정
-- 단, 파일명이 변경된다면 -> 그건 버전관리로 관리되어야 함
create table file_tb_ver(
                            file_vier_id bigserial primary key ,
                            file_id bigint,
                            file_name varchar(1024),
                            file_size bigint,
                            operation_description varchar(256),
                            mount_point varchar(512),
                            file_path varchar(1024),
                            modify_user int,
                            modify_time timestamp,
);
-- alter table file_tb_ver add constraint file_tb_ver_modify_user_fk foreign key (modify_user) references user_tb(user_id);
-- alter table file_tb_ver add constraint file_tb_ver_file_fk foreign key (file_id) references file_tb(file_id);

comment on table file_tb_ver is '파일 변경이력';
comment on column file_tb_ver.file_ver_id is '파일 변경이력 식별자';
comment on column file_tb_ver.file_name is '파일명';
comment on column file_tb_ver.file_size is '파일크기';
comment on column file_tb_ver.folder_id is '부모 폴더 식별자';
comment on column file_tb_ver.modify_user is '수정자';
comment on column file_tb_ver.modify_time is '수정시각';

-- file_stauts와 file_tb는 1:1 추가작업에 대한 이야기임 (검색엔진 동기화여부, 업로드완료여부 등등)
create table file_status_tb(
                               file_id bigserial primary key ,
                               upload_status int
);
alter table file_status_tb add constraint file_tb_ver_file_fk foreign key (file_id) references file_tb(file_id);

comment on table file_status_tb is '파일 작업 상태'
comment on column file_status_tb.file_id is '파일 식별자';
comment on column file_status_tb.upload_status is '압로드 상태';



--create table folder_acl_tb(
--    acl_id bigserial primary key ,
--    user_id int,
--    folder_id bigint,
--    access_type smallint, -- 1 -> 프라이빗폴더 대상 , 2 --> 팀 폴더 대상, 3 --> 공유폴더 대상
--    subject int, -- - READ, CREATE(==update 포함), DELETE, SAHRE 등등 가능한 기능과 권한에 대해 서술, ENUM으로 관리
--    create_time timestamp,
--    create_user int, -- 이 권한을 생성한 사람
--    is_delete smallint
--);

-- 개선안 : effect_type 추가, subject_type 추가
-- 원래 구조는 TF_1팀이 사업폴더에 접근권한을 받게되면 -> 모든 사람을 하나씩 다추가해줘야했음
-- 문제점 -> 새로 신입이 들어오면 수동으로 추가해줘함
-- 팀원이 1천명에 대해 "전사폴더" 같은거 만들면 1천 row가 추가됨
-- 팀 공유 자체를 캔슬하려면 -> "row 1000개 지워야함"
-- 따라서, effect_type : "ALLOW || DENY" (접근허가 / 명시적 접근거부)
-- subject -> subject_type: 팀을 대상으로 하는지, user를 대상으로 하는지 (TEAM || USER)
-- 기존의 "권한"을 표현하던 subject -> 컬럼명 변경 : permission
-- user_id -> subject_id : 대상의 PK를 저장
-- access_type -> is_shared : 굳이 (개인폴더인데->팀 공유), (팀폴더인데 ->팀공유) 를 enum으로 표현하면 enum 해석이 한번 더들어감. is_shared로 충분할것같음
create table folder_acl_tb(
                              acl_id bigserial primary key ,
                              user_id int,
                              folder_id bigint,
                              effect_type int, -- ALLOW || DENY (범위 공유 시, 명시적 거부대상 선언이 필요할경우 활용)
                              subject_type int, -- 대상 종류 (팀 || 사용자)
                              subject_id int, -- 대상의 PK
                              permission int,-- - READ, CREATE(==update 포함), DELETE, SAHRE 등등 가능한 기능과 권한에 대해 서술, ENUM으로 관리
                              is_shared smallint,
                              create_time timestamp,
                              create_user int, -- 이 권한을 생성한 사람
                              is_delete smallint
);

-- alter table folder_acl_tb add constraint folder_acl_tb_folder_fk foreign key (folder_id) references folder_tb(folder_id);
-- alter table folder_acl_tb add constraint folder_acl_tb_user_fk foreign key (user_id) references user_tb(user_id);
-- alter table folder_acl_tb add constraint folder_acl_tb_create_user_fk foreign key (create_user) references user_tb(user_id);

comment on table folder_acl_tb is '폴더 접근제어';
comment on column folder_acl_tb.acl_id is '접근제어 식별자';
comment on column folder_acl_tb.user_id is '피접근제어 대상';
comment on column folder_acl_tb.folder_id is '접근제어 폴더 식별자';
comment on column folder_acl_tb.effect_type is '접근제어 종류';
comment on column folder_acl_tb.subject_type is '피접근제어 종류';
comment on column folder_acl_tb.subject_id is '피 접근제어 식별자';
comment on column folder_acl_tb.permission is '제약 권한';
comment on column folder_acl_tb.is_shared is '공유로 생성된 제약 여부';
comment on column folder_acl_tb.create_time is '접근제어 생성시각';
comment on column folder_acl_tb.create_user is '제약 생성자';
comment on column folder_acl_tb.is_delete is '삭제여부';

-- 파일 접근권한 : file acl 검사 전, folder acl 검사, ->  해당 폴더에 접근권한이 있으면 직접 접근권한이 있는검임. 즉 file_acl 은 "공유" 로만 유효한 값을 가지게 될거임

-- 개선 내용 및 사유 folder_acl_tb와 완전동일

--create table file_acl_tb(
--    acl_id bigserial primary key ,
--    user_id int,
--    file_id bigint,
--    access_type smallint, -- 파일은 폴더권한이 있으면 무조건 있게될테니 단일 파일 공유를 통한 접근만 이 값이 유효할 예정
--    subject int, -- - READ, CREATE(==update 포함), DELETE, SAHRE 등등 가능한 기능과 권한에 대해 서술 (ENUM으로 관리)
--    create_time timestamp,
--    create_user int,
--    is_delete smallint
--)

create table file_acl_tb(
                            acl_id bigserial primary key ,
                            user_id int,
                            file_id bigint,
                            effect_type int, -- ALLOW || DENY (범위 공유 시, 명시적 거부대상 선언이 필요할경우 활용)
                            subject_type int, -- 대상 종류 (팀 || 사용자)
                            subject_id int, -- 대상의 PK
                            permission int,-- - READ, CREATE(==update 포함), DELETE, SAHRE 등등 가능한 기능과 권한에 대해 서술, ENUM으로 관리
                            is_shared smallint,
                            create_time timestamp,
                            create_user int,
                            is_delete smallint
)

-- alter table file_acl_tb add constraint file_acl_tb_user_fk foreign key (user_id) references user_tb(user_id);
-- alter table file_acl_tb add constraint file_acl_tb_create_user_fk foreign key (create_user) references user_tb(user_id);
-- alter table file_acl_tb add constraint file_acl_tb_file_fk foreign key (file_id) references file_tb(file_id);

comment on table file_acl_tb is '파일 접근제어';
comment on column file_acl_tb.acl_id is '접근제어 식별자';
comment on column file_acl_tb.user_id is '피접근제어 대상';
comment on column file_acl_tb.file_id is '접근제어 파일 식별자';
comment on column file_acl_tb.effect_type is '접근제어 종류';
comment on column file_acl_tb.subject_type is '피접근제어 종류';
comment on column file_acl_tb.subject_id is '피 접근제어 식별자';
comment on column file_acl_tb.permission is '제약 권한';
comment on column file_acl_tb.is_shared is '공유로 생성된 제약 여부';
comment on column file_acl_tb.create_time is '접근제어 생성시각'
comment on column file_acl_tb.create_user is '제약 생성자'
comment on column file_acl_tb.is_delete is '삭제여부'

