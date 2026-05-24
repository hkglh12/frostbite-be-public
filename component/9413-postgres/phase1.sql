-- 실 사용시, FK 제약은 모두 해제한다. Application Level에서 정합성/무결성 검증을 수행한다. (DROP CASCADE 등 제어가 복잡해지기 때문)


create table user_tb(
                        user_id serial primary key ,
                        name varchar(128),
                        email varchar(256),
                        password varchar(1024),
                        phone_number varchar(14),
                        birth date,
                        create_date date,
                        uuid varchar(50),
                        account_type smallint,
                        status smallint
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
comment on column user_tb.account_type is '계정 권한 종류[관리자||사용자]';
comment on column user_tb.status is '계정상태';

create table team_tb(
                        team_id serial primary key ,
                        name varchar(256),
                        create_time timestamp,
                        create_user int,
                        modify_time timestamp,
                        modify_user int,
                        uuid varchar(50),
                        status smallint
);

alter table team_tb add constraint team_tb_user_fk foreign key (create_user) references user_tb (user_id);

comment on table team_tb is '팀';
comment on column team_tb.team_id is '팀 식별자'
comment on column team_tb.name is '팀 이름';
comment on column team_tb.create_time is '생성시각';
comment on column team_tb.create_user is '생성자';
comment on column team_tb.uuid is '팀 UUID';
comment on column team_tb.status is '팀상태';

create table rel_user_team_tb(
                              rel_id serial primary key ,
                              user_id int,
                              team_id int,
                              create_user int,
                              join_time timestamp,
                              role smallint,
                              status smallint
);

alter table rel_user_team_tb add constraint rel_user_team_tb_user_fk foreign key (user_id) references user_tb (user_id);
alter table rel_user_team_tb add constraint rel_user_team_tb_create_user_fk foreign key (user_id) references user_tb (user_id);
alter table rel_user_team_tb add constraint rel_user_team_tb_team_fk foreign key (team_id) references team_tb (team_id);

comment on table rel_user_team_tb is '사용자-팀 관계';
comment on column rel_user_team_tb.rel_id is '사용자-팀 관계식별자';
comment on column rel_user_team_tb.user_id is '사용자 식별자';
comment on column rel_user_team_tb.create_user is '초대자';
comment on column rel_user_team_tb.team_id is '팀 식별자';
comment on column rel_user_team_tb.join_time is '합류시각';
comment on column rel_user_team_tb.role is '팀 내 역할';
comment on column rel_user_team_tb.status is '관계상태';

/* 원래 전역 역할을 표현하기 위함이었으나, ADMIN과 USER밖에 없음
   굳이 테이블을 나누어서 표현하는건 과하다고 판단, ENUM으로 전환 */
/*
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

alter table rel_user_role_tb add constraint rel_user_role_tb_user_fk foreign key (user_id) references user_tb (user_id);
alter table rel_user_role_tb add constraint rel_user_role_tb_role_fk foreign key (role_id) references role_tb (role_id);

comment on table rel_user_role_tb is '사용자-역할 관계';
comment on column rel_user_role_tb.rel_id is '관계 식별자';
comment on column rel_user_role_tb.user_id is '사용자 식별자';
comment on column rel_user_role_tb.role_id is '역할 식별자';

 */

create table folder_tb
(
    uuid                   varchar(50),
    folder_id              bigserial
        primary key,
    name                   varchar(512),
    folder_type            integer,
    parent_folder_id       bigint,
    resource_scope_team_id integer,
    resource_scope_user_id integer,
    resource_owner_id      integer,
    access_policy           integer,
    folder_id_path         varchar,
    folder_name_path       varchar,
    render_sequence        integer,
    status                 smallint,
    create_user_id         integer,
    create_time            timestamp,
    modify_user_id         integer,
    modify_time            timestamp,
    delete_user_id         integer,
    delete_time            timestamp

);

comment on table folder_tb is '폴더';
comment on column folder_tb.uuid is 'uuid';
comment on column folder_tb.folder_id is '폴더 식별자';
comment on column folder_tb.name is '폴더명';
comment on column folder_tb.folder_type is '폴더 종류(PRIVATE||TEAM)';
comment on column folder_tb.parent_folder_id is '부모 폴더 식별자';
comment on column folder_tb.resource_scope_team_id is '소유 팀 식별자';
comment on column folder_tb.resource_scope_user_id is '소유 사용자식별자';
comment on column folder_tb.resource_owner_id is '폴더 실제 관리 사용자 (UserPK)';
comment on column folder_tb.access_policy is '폴더접근정책(USER||TEAM)';
comment on column folder_tb.folder_id_path is ' 폴더 식별자 경로 ';
comment on column folder_tb.folder_name_path is '폴더 이름 경로';
comment on column folder_tb.render_sequence is '출력순서';
comment on column folder_tb.status is '폴더상태';
comment on column folder_tb.create_user_id is '폴더 생성자 PK';
comment on column folder_tb.create_time is '생성시각';
comment on column folder_tb.modify_user_id is '폴더 수정자 PK';
comment on column folder_tb.modify_time is '수정시각';
comment on column folder_tb.delete_user_id is '삭제자 PK';
comment on column folder_tb.delete_time is '삭제시각';

alter table folder_tb add constraint folder_tb_parent_fk foreign key (parent_folder_id) references folder_tb(folder_id);
alter table folder_tb add constraint folder_tb_scope_team_fk foreign key (resource_scope_team_id) references team_tb(team_id);
alter table folder_tb add constraint folder_tb_scope_user_fk foreign key (resource_scope_user_id) references user_tb(user_id);
alter table folder_tb add constraint folder_tb_c_user_fk foreign key (create_user_id) references user_tb(user_id);
alter table folder_tb add constraint folder_tb_m_user_fk foreign key (modify_user_id) references user_tb(user_id);
alter table folder_tb add constraint folder_tb_d_user_fk foreign key (delete_user_id) references user_tb(user_id);

ALTER TABLE folder_tb
    ADD CONSTRAINT chk_folder_resource_scope
        CHECK (
            (folder_type = 1 AND resource_scope_user_id IS NOT NULL AND resource_scope_team_id IS NULL)
                OR (folder_type = 2 AND resource_scope_user_id IS NULL AND resource_scope_team_id IS NOT NULL)
            );

ALTER TABLE folder_tb
    ALTER COLUMN folder_type SET NOT NULL;
ALTER TABLE folder_tb
    ALTER COLUMN access_policy SET NOT NULL;

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


alter table file_tb add constraint file_tb_folder_fk  foreign key (folder_id) references folder_tb(folder_id);
alter table file_tb add constraint file_tb_create_user_fk foreign key (create_user) references user_tb(user_id);
alter table file_tb add constraint file_tb_modify_user_fk foreign key (modify_user) references user_tb(user_id);

-- file_stauts와 file_tb는 1:1 추가작업에 대한 이야기임 (검색엔진 동기화여부, 업로드완료여부 등등)
create table file_status_tb(
                               file_id bigserial primary key ,
                               upload_status int
);
alter table file_status_tb add constraint file_tb_ver_file_fk foreign key (file_id) references file_tb(file_id);

comment on table file_status_tb is '파일 작업 상태';
comment on column file_status_tb.file_id is '파일 식별자';
comment on column file_status_tb.upload_status is '압로드 상태';


create table folder_acl_tb
(
    acl_id              bigserial
        primary key,
    folder_id           bigint,
    subject_type        smallint,
    effect_type         smallint,
    subject_team_id          integer,
    subject_user_id         integer,
    permission_bit_mask smallint,
    status              smallint,
    create_time         timestamp,
    create_user_id      integer,
    modify_time         timestamp,
    modify_user_id      integer,
    delete_time         timestamp,
    delete_user_id       integer
);

comment on table folder_acl_tb is '폴더 접근제어';
comment on column folder_acl_tb.acl_id is '접근제어 식별자';
comment on column folder_acl_tb.folder_id is '접근제어 폴더 식별자';
comment on column folder_acl_tb.effect_type is '접근제어 종류(허용||거부)';
comment on column folder_acl_tb.subject_type is '접근제어 적용 대상 종류 (팀||사용자)';
comment on column folder_acl_tb.subject_team_id is '적용 접근제어 대상 팀 식별자';
comment on column folder_acl_tb.subject_user_id is '적용 접근제어 대상 사용자 식별자';
comment on column folder_acl_tb.permission_bit_mask is '동작권한';
comment on column folder_acl_tb.create_time is '접근제어 생성시각';
comment on column folder_acl_tb.create_user_id is '제약 생성자';
comment on column folder_acl_tb.status is '삭제여부';

alter table folder_acl_tb add constraint folder_acl_folder_fk foreign key (folder_id) references folder_tb (folder_id);

ALTER TABLE folder_acl_tb
    ADD CONSTRAINT chk_folder_acl_subject
        CHECK (
            (subject_type = 1 AND subject_user_id IS NOT NULL AND subject_team_id IS NULL)
                OR (subject_type = 2 AND subject_user_id IS NULL AND subject_team_id IS NOT NULL)
            );
ALTER TABLE folder_acl_tb
    ADD CONSTRAINT fk_folder_acl_subject_user
        FOREIGN KEY (subject_user_id)
            REFERENCES user_tb(user_id);

ALTER TABLE folder_acl_tb
    ADD CONSTRAINT fk_folder_acl_subject_team
        FOREIGN KEY (subject_team_id)
            REFERENCES team_tb(team_id);

alter table folder_acl_tb add constraint folder_acl_c_user_fk foreign key (create_user_id) references user_tb (user_id);
alter table folder_acl_tb add constraint folder_acl_m_user_fk foreign key (modify_user_id) references user_tb (user_id);
alter table folder_acl_tb add constraint folder_acl_d_user_fk foreign key (delete_user_id) references user_tb (user_id);

ALTER TABLE folder_acl_tb
    ALTER COLUMN folder_id SET NOT NULL,
    ALTER COLUMN subject_type SET NOT NULL,
    ALTER COLUMN effect_type SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE folder_acl_tb
    ADD CONSTRAINT chk_folder_acl_subject
        CHECK (
            (subject_type = 1 AND subject_user_id IS NOT NULL AND subject_team_id IS NULL)
                OR (subject_type = 2 AND subject_user_id IS NULL AND subject_team_id IS NOT NULL)
            );

-- DENY일떄 permissionBitMask가 적용 되면 안됨을 제약 표현
ALTER TABLE folder_acl_tb
    ADD CONSTRAINT chk_folder_acl_effect_permission
        CHECK (
            (effect_type = 1 AND permission_bit_mask IS NOT NULL)
                OR (effect_type = 2 AND permission_bit_mask IS NULL)
            );

ALTER TABLE folder_acl_tb
    ADD CONSTRAINT chk_folder_acl_effect_permission
        CHECK (
            (effect_type = 1 AND permission_bit_mask IS NOT NULL)
                OR (effect_type = 2 AND permission_bit_mask IS NULL)
            );

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

alter table file_acl_tb add constraint file_acl_tb_user_fk foreign key (user_id) references user_tb(user_id);
alter table file_acl_tb add constraint file_acl_tb_create_user_fk foreign key (create_user) references user_tb(user_id);
alter table file_acl_tb add constraint file_acl_tb_file_fk foreign key (file_id) references file_tb(file_id);

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

