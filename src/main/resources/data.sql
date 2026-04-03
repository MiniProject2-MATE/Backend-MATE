-- [1] 유저 생성 (ID 1, 2, 3)
INSERT INTO users (user_id, email, password, nickname, position, phone_number, created_at) VALUES
(1, 'userbaek@mate.com', '$2a$10$dummy', '백승호', 'BE', '01011111111', NOW()),
(2, 'user1@mate.com', '$2a$10$dummy', '개발왕', 'FE', '01022222222', NOW()),
(3, 'user2@mate.com', '$2a$10$dummy', '스프링러너', 'BE', '01033333333', NOW());

-- [2] 유저별 기술 스택 연계 (UserTechStack 테이블)
INSERT INTO user_tech_stacks (user_id, tech_stack) VALUES
(1, 'Spring Boot'), (1, 'Java'), (1, 'MySQL'), -- 1번: 백엔드 풀세트
(2, 'React'), (2, 'TypeScript'), (2, 'Next.js'), -- 2번: 프론트엔드 풀세트
(3, 'Spring Boot'), (3, 'Kotlin'), (3, 'JPA');     -- 3번: 최신 백엔드 스택

-- [3] 프로젝트 생성
-- 1번(백승호)의 프로젝트: 백엔드 위주
INSERT INTO projects (project_id, owner_id, category, title, content, recruit_count, current_count, on_offline, status, end_date, created_at) VALUES
(1, 1, 'PROJECT', 'Spring Cloud 기반 마이크로서비스', 'MSA 구축하실 분', 5, 1, 'ONLINE', 'RECRUITING', '2026-12-31', NOW());

-- 2번(개발왕)의 프로젝트: 프론트 위주
INSERT INTO projects (project_id, owner_id, category, title, content, recruit_count, current_count, on_offline, status, end_date, created_at) VALUES
(2, 2, 'STUDY', 'React 디자인 패턴 스터디', '고급 패턴 같이 공부해요', 4, 1, 'BOTH', 'RECRUITING', '2026-06-15', NOW());

-- [4] 활동 이력 연계 (참여 및 신청)
-- 2번(개발왕)이 1번(MSA 프로젝트)에 참여 승인됨 (FE 담당으로 참여하는 시나리오)
INSERT INTO applications (project_id, applicant_id, message, status, applied_at)
VALUES (1, 2, '프론트엔드 작업 도와드리고 싶습니다!', 'ACCEPTED', NOW());
INSERT INTO project_members (project_id, user_id, role, joined_at)
VALUES (1, 2, 'MEMBER', NOW());

-- 3번(스프링러너)이 2번(React 스터디)에 지원 중 (BE지만 FE 배우고 싶은 시나리오)
INSERT INTO applications (project_id, applicant_id, message, status, applied_at)
VALUES (2, 3, '백엔드 개발자인데 리액트 기초부터 배우고 싶어요.', 'PENDING', NOW());

-- 1번(백승호)이 2번(React 스터디)에 지원했다가 거절됨
INSERT INTO applications (project_id, applicant_id, message, status, applied_at)
VALUES (2, 1, '참여 희망합니다.', 'REJECTED', NOW());
INSERT INTO users (
    created_at,
    updated_at,
    deleted_at,
    nickname,
    phone_number,
    email,
    password,
    profile_img,
    position,
    role
) VALUES (
    NOW(),
    NULL,
    NULL,
    '관리자',
    '01099999999',
    'admin@mate.com',
    '$2a$10$aBb/e9umQqst4mwOfyF14u.RXqesvKXPQHjJ763He8g52Ojk25EVS',
    NULL,
    'BE',
    'ROLE_ADMIN'
);