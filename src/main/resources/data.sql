-- 1. 유저 생성 (비밀번호: 암호화된 더미값)
INSERT INTO users (email, password, nickname, position, phone_number, created_at) VALUES
('admin@mate.com', '$2a$10$dummy', '관리자', 'BE', '010-1234-5678', NOW()),
('user1@mate.com', '$2a$10$dummy', '개발왕', 'FE', '010-1111-2222', NOW()),
('user2@mate.com', '$2a$10$dummy', '스프링러너', 'BE', '010-3333-4444', NOW());

-- 2. 기술 스택 매핑
INSERT INTO user_tech_stacks (user_id, tech_stack) VALUES
(2, 'React'), (2, 'TypeScript'),
(3, 'Spring Boot'), (3, 'Java');

-- 3. 프로젝트 생성 (user1이 생성한 React 스터디)
INSERT INTO projects (owner_id, category, title, content, recruit_count, current_count, on_offline, status, end_date, created_at) VALUES
(2, 'STUDY', '리액트 기초 스터디원 구합니다', '주 2회 온라인 스터디...', 4, 1, 'ONLINE', 'RECRUITING', '2026-04-10', NOW());

-- 4. 프로젝트 멤버 (방장 자동 등록)
INSERT INTO project_members (project_id, user_id, role, joined_at) VALUES
(1, 2, 'OWNER', NOW());

-- 5. 지원서 작성 (user3이 프로젝트에 지원)
INSERT INTO applications (project_id, applicant_id, message, status, applied_at) VALUES
(1, 3, '안녕하세요 백엔드 개발자지만 프론트도 배우고 싶습니다!', 'PENDING', NOW());