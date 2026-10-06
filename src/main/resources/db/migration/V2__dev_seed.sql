-- DEV SEED ONLY (Da Lat). Fake ids/prices/video ids; coordinates are approximate.
-- Counts are kept consistent with the seeded mentions: 3 videos by 3 distinct authors.

INSERT INTO video (tiktok_id, web_video_url, author_hash, created_at, play_count, like_count, save_count, comment_count, share_count, language, embed_status) VALUES
('7300000000000000001', 'https://www.tiktok.com/@seed/video/7300000000000000001', encode(sha256('seed-author-a'::bytea), 'hex'), '2026-03-12 09:00:00+07', 250000, 18000, 6200, 410, 900, 'vi', 'ok'),
('7300000000000000002', 'https://www.tiktok.com/@seed/video/7300000000000000002', encode(sha256('seed-author-b'::bytea), 'hex'), '2026-05-02 18:30:00+07', 920000, 61000, 24000, 1300, 5200, 'vi', 'ok'),
('7300000000000000003', 'https://www.tiktok.com/@seed/video/7300000000000000003', encode(sha256('seed-author-c'::bytea), 'hex'), '2026-08-21 12:15:00+07',  80000,  5200,  1900,  120,  240, 'vi', 'unknown');

INSERT INTO place (id, name, category, city, address, geom, goong_place_id, energy, tags, best_for, price_min, price_max, quality_score, mention_count, creator_count, resolve_status) VALUES
('a0000000-0000-4000-8000-000000000001', 'Hồ Xuân Hương', 'play', 'Đà Lạt', 'Phường 1, Đà Lạt, Lâm Đồng',
 ST_SetSRID(ST_MakePoint(108.4419, 11.9404), 4326)::geography, 'seed-goong-0001', 0.15,
 '{lake,walk,chill,sunset}', '{couple,solo,family}', 0, 0, 0.90, 2, 2, 'matched'),
('a0000000-0000-4000-8000-000000000002', 'Đồi chè Cầu Đất', 'play', 'Đà Lạt', 'Xã Xuân Trường, Đà Lạt, Lâm Đồng',
 ST_SetSRID(ST_MakePoint(108.5560, 11.8620), 4326)::geography, 'seed-goong-0002', 0.25,
 '{tea,view,sunrise,chill,photo}', '{couple,friends,solo}', 50000, 100000, 0.85, 2, 2, 'matched'),
('a0000000-0000-4000-8000-000000000003', 'Cà phê Đồi Thông Xanh', 'eat', 'Đà Lạt', 'Phường 3, Đà Lạt, Lâm Đồng',
 ST_SetSRID(ST_MakePoint(108.4600, 11.9650), 4326)::geography, 'seed-goong-0003', 0.20,
 '{cafe,chill,pine-forest,photo}', '{couple,solo}', 45000, 90000, 0.70, 1, 1, 'matched'),
('a0000000-0000-4000-8000-000000000004', 'Bánh tráng nướng chợ đêm Đà Lạt', 'eat', 'Đà Lạt', 'Chợ Đà Lạt, Phường 1, Đà Lạt, Lâm Đồng',
 ST_SetSRID(ST_MakePoint(108.4378, 11.9429), 4326)::geography, 'seed-goong-0004', 0.60,
 '{street-food,night-market,cheap,local}', '{friends,solo}', 15000, 40000, 0.78, 3, 3, 'matched'),
('a0000000-0000-4000-8000-000000000005', 'Canyoning thác Datanla', 'play', 'Đà Lạt', 'Đèo Prenn, Phường 3, Đà Lạt, Lâm Đồng',
 ST_SetSRID(ST_MakePoint(108.4503, 11.9014), 4326)::geography, 'seed-goong-0005', 0.95,
 '{adventure,canyoning,adrenaline,outdoor}', '{friends,solo}', 600000, 900000, 0.88, 2, 2, 'matched');

INSERT INTO mention (video_id, place_id, name_raw, category, sentiment, sponsored_signal, evidence, prompt_version, model, source_version) VALUES
('7300000000000000001', 'a0000000-0000-4000-8000-000000000001', 'hồ Xuân Hương', 'play', 'positive', false, '{"t_start_sec": 15, "snippet": "sáng sớm đi dạo quanh hồ Xuân Hương rất chill"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000001', 'a0000000-0000-4000-8000-000000000002', 'đồi chè Cầu Đất', 'play', 'positive', false, '{"t_start_sec": 95, "snippet": "săn mây ở đồi chè Cầu Đất"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000001', 'a0000000-0000-4000-8000-000000000004', 'bánh tráng nướng', 'eat', 'positive', false, '{"t_start_sec": 140, "snippet": "tối ra chợ ăn bánh tráng nướng, 25k một cái"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000002', 'a0000000-0000-4000-8000-000000000002', 'Cầu Đất', 'play', 'positive', false, '{"t_start_sec": 30, "snippet": "vé vào đồi chè khoảng bảy chục nghìn"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000002', 'a0000000-0000-4000-8000-000000000003', 'cà phê đồi thông', 'eat', 'positive', false, '{"t_start_sec": 75, "snippet": "quán cà phê view đồi thông yên tĩnh"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000002', 'a0000000-0000-4000-8000-000000000004', 'bánh tráng nướng chợ đêm', 'eat', 'neutral', false, '{"t_start_sec": 160, "snippet": "đông lắm, phải xếp hàng"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000002', 'a0000000-0000-4000-8000-000000000005', 'canyoning Datanla', 'play', 'positive', true, '{"t_start_sec": 210, "snippet": "tour canyoning giá bảy trăm năm chục nghìn"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000003', 'a0000000-0000-4000-8000-000000000001', 'Hồ Xuân Hương', 'play', 'positive', false, '{"t_start_sec": 40, "snippet": "chiều tà ngồi cạnh hồ"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000003', 'a0000000-0000-4000-8000-000000000005', 'thác Datanla', 'play', 'positive', false, '{"t_start_sec": 55, "snippet": "nhảy thác adrenaline cực mạnh"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000003', 'a0000000-0000-4000-8000-000000000004', 'bánh tráng nướng', 'eat', 'positive', false, '{"t_start_sec": 120, "snippet": "ăn vặt chợ đêm"}', 'seed-v1', 'seed', 'seed'),
('7300000000000000003', NULL, 'quán cà phê gì đó trên đường lên đèo', 'eat', 'neutral', false, '{"t_start_sec": 170, "snippet": "ghé một quán cà phê trên đường lên đèo"}', 'seed-v1', 'seed', 'seed');

INSERT INTO place_video (place_id, video_id, rank, t_start_sec) VALUES
('a0000000-0000-4000-8000-000000000001', '7300000000000000001', 1, 15),
('a0000000-0000-4000-8000-000000000001', '7300000000000000003', 2, 40),
('a0000000-0000-4000-8000-000000000002', '7300000000000000002', 1, 30),
('a0000000-0000-4000-8000-000000000002', '7300000000000000001', 2, 95),
('a0000000-0000-4000-8000-000000000003', '7300000000000000002', 1, 75),
('a0000000-0000-4000-8000-000000000004', '7300000000000000002', 1, 160),
('a0000000-0000-4000-8000-000000000004', '7300000000000000001', 2, 140),
('a0000000-0000-4000-8000-000000000004', '7300000000000000003', 3, 120),
('a0000000-0000-4000-8000-000000000005', '7300000000000000002', 1, 210),
('a0000000-0000-4000-8000-000000000005', '7300000000000000003', 2, 55);

INSERT INTO place_fact (place_id, type, value, evidence_video_id, t_start_sec, observed_at) VALUES
('a0000000-0000-4000-8000-000000000001', 'tip', '{"text": "Đi sáng sớm hoặc chiều tà để tránh nắng và đông người."}', '7300000000000000001', 15, '2026-03-12 09:00:00+07'),
('a0000000-0000-4000-8000-000000000002', 'price_median', '{"amount": 70000, "currency": "VND"}', '7300000000000000002', 30, '2026-05-02 18:30:00+07'),
('a0000000-0000-4000-8000-000000000002', 'tip', '{"text": "Đến trước 6 giờ sáng để săn mây."}', '7300000000000000001', 95, '2026-03-12 09:00:00+07'),
('a0000000-0000-4000-8000-000000000002', 'warning', '{"text": "Đường lên dốc và trơn khi trời mưa."}', '7300000000000000002', 35, '2026-05-02 18:30:00+07'),
('a0000000-0000-4000-8000-000000000003', 'price_median', '{"amount": 60000, "currency": "VND"}', '7300000000000000002', 75, '2026-05-02 18:30:00+07'),
('a0000000-0000-4000-8000-000000000004', 'price_median', '{"amount": 25000, "currency": "VND"}', '7300000000000000001', 140, '2026-03-12 09:00:00+07'),
('a0000000-0000-4000-8000-000000000004', 'warning', '{"text": "Giờ cao điểm phải xếp hàng khá lâu."}', '7300000000000000002', 160, '2026-05-02 18:30:00+07'),
('a0000000-0000-4000-8000-000000000005', 'price_median', '{"amount": 750000, "currency": "VND"}', '7300000000000000002', 210, '2026-05-02 18:30:00+07'),
('a0000000-0000-4000-8000-000000000005', 'warning', '{"text": "Cần đặt trước; có giới hạn độ tuổi và sức khỏe."}', '7300000000000000003', 55, '2026-08-21 12:15:00+07');
