-- [1] 유저 생성 (ID 1, 2, 3, 4, 5, 6, 7)
-- 비밀번호 해시는 평문 'test1234!' 입니다. ($2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m)
INSERT INTO users (user_id, email, password, nickname, position, phone_number, role, created_at, updated_at) VALUES
(1, 'userbaek@mate.com', '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m', '백승호', 'BE', '01011111111', 'ROLE_USER', NOW(), NOW()),
(2, 'user1@mate.com', '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m', '개발왕', 'FE', '01022222222', 'ROLE_USER', NOW(), NOW()),
(3, 'user2@mate.com', '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m', '스프링', 'BE', '01033333333', 'ROLE_USER', NOW(), NOW()),
(4, 'designer1@mate.com', '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m', '디자이너', 'DE', '01044444444', 'ROLE_USER', NOW(), NOW()),
(5, 'pm1@mate.com', '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m', '피엠', 'PM', '01055555555', 'ROLE_USER', NOW(), NOW()),
(6, 'user3@mate.com', '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m', '리액트초보', 'FE', '01066666666', 'ROLE_USER', NOW(), NOW()),
(7, 'user4@mate.com', '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m', '데마', 'DE', '01077777777', 'ROLE_USER', NOW(), NOW());

-- [2] 유저별 기술 스택 연계
INSERT INTO user_tech_stacks (user_id, tech_stack) VALUES
(1, 'Spring Boot'), (1, 'Java'), (1, 'MySQL'),
(2, 'React'), (2, 'TypeScript'), (2, 'Next.js'),
(3, 'Spring Boot'), (3, 'Kotlin'), (3, 'JPA'),
(4, 'Figma'), (4, 'Adobe XD'),
(5, 'Jira'), (5, 'Confluence'),
(6, 'React'), (6, 'JavaScript'),
(7, 'Python'), (7, 'SQL');

-- [3] 프로젝트 생성
INSERT INTO projects (project_id, owner_id, category, title, content, recruit_count, current_count, on_offline, status, end_date, created_at, updated_at) VALUES
(1, 1, 'PROJECT', 'Spring Cloud 기반 마이크로서비스', 'MSA 구축하실 분', 5, 1, 'ONLINE', 'RECRUITING', '2026-12-31', NOW(), NOW()),
(2, 2, 'STUDY', 'React 디자인 패턴 스터디', '고급 패턴 같이 공부해요', 4, 1, 'BOTH', 'RECRUITING', '2026-06-15', NOW(), NOW());

-- [4] 활동 이력 연계 (참여 및 신청)
-- applications 테이블 (updated_at 추가)
INSERT INTO applications (project_id, applicant_id, message, status, applied_at, created_at, updated_at)
VALUES (1, 2, '프론트엔드 작업 도와드리고 싶습니다!', 'ACCEPTED', NOW(), NOW(), NOW()),
       (2, 3, '백엔드 개발자인데 리액트 기초부터 배우고 싶어요.', 'PENDING', NOW(), NOW(), NOW()),
       (2, 1, '참여 희망합니다.', 'REJECTED', NOW(), NOW(), NOW());

-- project_members 테이블 (updated_at 추가)
INSERT INTO project_members (project_id, user_id, role, joined_at, created_at, updated_at)
VALUES (1, 2, 'MEMBER', NOW(), NOW(), NOW());

-- 관리자 계정 생성
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
    NOW(),
    NULL,
    '관리자',
    '01099999999',
    'admin@mate.com',
    '$2a$10$AU2MaeDv7z2LK3dIyYQ3WO5NzWMB03QwyTGdkJXpRS5vEoz7Q9L8m',
    NULL,
    'BE',
    'ROLE_ADMIN'
);