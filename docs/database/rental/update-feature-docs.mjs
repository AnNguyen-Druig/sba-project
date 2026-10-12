// Apply approved descriptions while retaining feature IDs, assignments and difficulty scores.
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const dir=path.dirname(fileURLToPath(import.meta.url));
const changes=JSON.parse(fs.readFileSync(path.join(dir,'feature-overrides.json'),'utf8'));
for(const name of ['be_tasks.md','all-feature.txt']){
 const file=path.resolve(dir,'../..',name);let s=fs.readFileSync(file,'utf8');
 const ids=x=>[...x.matchAll(/\[(P[123]-\d+)\]/g)].map(m=>m[1]);const before=ids(s);
 s=s.split(/\r?\n/).map(line=>{
  if(!/^(?:- \[ \] \*\*\[P|\[P|- \[BR|\[BR)/.test(line))return line;
  const m=line.match(/^(.*?\[(P[123]-\d+|BR-\d+)\](?:\*\*)?\s*)(.*)$/);
  return !m||!changes[m[2]]?line:m[1]+changes[m[2]]+(line.match(/\s+_\(độ khó.*$/)?.[0]??'');
 }).join('\n');
 const replacements=[
 ['Booking/cọc hợp đồng','Booking Hold/PREPAID_RENT'],
 ['Invoice – công nợ – Payment – đối soát VietQR','Invoice – công nợ – Payment – QR thủ công/Sandbox'],
 ['đối soát ngân hàng, công nợ','xác minh thanh toán, công nợ'],
 ['Tài khoản nhận tiền của Owner và phát hành VietQR','QR theo Branch và tham chiếu thanh toán'],
 ['Đối soát VietQR thực tế Phase 1; chống webhook giả/lặp; chỉ ghi nhận đúng hóa đơn/booking.','QR thủ công hoặc sandbox đã chọn; chống callback giả/lặp khi dùng sandbox; ghi nhận đúng Invoice/Booking.'],
 ['xác nhận thủ công chỉ là ngoại lệ có audit.','xác nhận thủ công là luồng hợp lệ của QR thủ công và luôn có audit.'],
 ['Mặc định chia đều; Tenant đề xuất thay đổi','Mặc định theo ngày cư trú; Tenant đề xuất thay đổi'],
 ['thanh toán cọc thực →','xác minh Booking Hold đúng môi trường →'],
 ['Đối soát VietQR chỉ dùng dữ liệu giao dịch đã xác thực;','QR thủ công/Sandbox chỉ dùng Payment đã xác minh;'],
 ['Logical ERD_Ver2 trong All_Diagrams.drawio (24 entity hiện tại).','Logical ERD_Ver2 và các trang Physical trong All_Diagrams.drawio; các trang Legacy chỉ để đối chiếu.']
 ];for(const [a,b] of replacements)s=s.replaceAll(a,b);
 s=s.replace(/\btenant_id\b/g,'tenant_user_id');
 s=s.replace(/^- Tên nhóm nghiệp vụ bám sát các thực thể hiện hữu:.*$/m,'- Thiết kế hiện hành: 34 bảng trong database/rental/TABLE_CATALOG.md. Hồ sơ cá nhân ở USERS, nghĩa vụ thuê ở CONTRACT_TENANTS; MANAGER/TENANT là vai trò/ngữ cảnh nghiệp vụ, không bảng hồ sơ đăng nhập riêng.');
 if(!s.includes('Điều chỉnh chính thức: 34 bảng')){
  const note='Điều chỉnh chính thức: 34 bảng (32 + 1 + 1), thay thế phần lưu trữ D04/D18/D33/D39 trước đây. USERS là hồ sơ duy nhất; CONTRACT_TENANTS tham chiếu tenant_user_id. Các task DEFERRED giữ ID và điểm ước lượng gốc để truy vết; tổng 211 mã/709 điểm không phải cam kết triển khai toàn bộ phạm vi hiện tại. Xem database/rental/REFACTOR_REPORT.md.\n\n';
  const first=s.indexOf('\n');s=s.slice(0,first+1)+'\n'+note+s.slice(first+1);
 }
 s=s.replace(/^- \*\*Chưa chốt:\*\*.*$/m,'- **Đã chốt D01–D40:** N–N roles, Contract chính theo Room, nghĩa vụ/Invoice theo ContractTenant, chia phí theo ngày cư trú, PREPAID_RENT tháng đầu/cuối và QR thủ công hoặc Sandbox. Các mức tiền/thời hạn và xử lý rời sớm còn mở.');
 s=s.replace(/^- Việc cho một tài khoản mang NHIỀU ROLE.*$/m,'- Đã chốt: USERS N–N ROLES qua USER_ROLES; USER/OWNER/MANAGER/TENANT/ADMIN, không có role GUEST.');
 s=s.replace(/^- Một người có thể mang nhiều role đồng thời.*$/m,'- Đã chốt D03: một User có nhiều role đồng thời; không hỏi lại.');
 s=s.replace(/^- Cách tính số người ở thực tế trong kỳ khi chia điện\/nước.*$/m,'- Đã chốt D22: số ngày cư trú giao với kỳ/segment và làm tròn VND xác định.');
 s=s.replace(/^- Ai được chốt\/phát hành hóa đơn;.*$/m,'- Đã chốt D26: Owner/Manager phát hành; Owner duyệt điều chỉnh sau phát hành, bảo toàn snapshot và audit.');
 s=s.replace(/^- Quy tắc tiền chuyển thừa\/thiếu, đóng hộ,.*$/m,'- Đã chốt D25/D29: phân biệt payer/người hưởng; tiền dư/chưa khớp nằm tại Payment. Thời hạn xử lý ngoại lệ cụ thể còn cấu hình.');
 if(!s.includes('Cập nhật thiết kế D01–D40: 2026-10-12')){
  const note='Cập nhật thiết kế D01–D40: 2026-10-12 — xem database/rental/REFACTOR_REPORT.md. Giữ nguyên 211 feature ID; DDL chưa đồng nghĩa task backend đã hoàn thành. “Cọc giữ chỗ” là Booking Hold; hợp đồng dùng PREPAID_RENT, không SECURITY_DEPOSIT. Quyết định mới thay thế các mô tả cũ mâu thuẫn.\n\n';
  const first=s.indexOf('\n');s=s.slice(0,first+1)+'\n'+note+s.slice(first+1);
 }
 if(JSON.stringify(before)!==JSON.stringify(ids(s)))throw Error('Feature IDs changed: '+name);
 fs.writeFileSync(file,s);console.log('Updated descriptions; feature IDs preserved: '+name);
}
