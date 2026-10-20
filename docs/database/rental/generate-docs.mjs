// Generate documentation/ERD from the catalog exported by the executed PostgreSQL DDL.
// No database connection. Preserves the three pre-refactor pages as Legacy pages.
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const dir=path.dirname(fileURLToPath(import.meta.url)),docs=path.resolve(dir,'../..');
const db=JSON.parse(fs.readFileSync(path.join(dir,'schema-catalog.json'),'utf8'));
const groups={
  "identity": [
    "users",
    "roles",
    "user_roles",
    "owner_managers"
  ],
  "property": [
    "branches",
    "branch_policies",
    "room_types",
    "rooms",
    "room_slots"
  ],
  "leasing": [
    "appointments",
    "bookings",
    "contracts",
    "contract_tenants",
    "resource_allocations",
    "room_slot_occupancies",
    "contract_amendments"
  ],
  "utilities": [
    "services",
    "room_services",
    "contract_services",
    "room_service_charges",
    "cost_allocation_proposals"
  ],
  "billing": [
    "invoices",
    "invoice_items"
  ],
  "payment": [
    "payments",
    "payment_webhook_events"
  ],
  "maintenance": [
    "furnitures",
    "furniture_warranties",
    "report_requests",
    "report_requests_services",
    "report_requests_furnitures",
    "report_records"
  ],
  "notification": [
    "notifications"
  ],
  "community": [
    "threads"
  ],
  "common.audit": [
    "audit_logs"
  ]
};
const purpose={"users":"Tài khoản đăng nhập duy nhất","roles":"Danh mục 5 role ứng dụng","user_roles":"User N–N Role","owner_managers":"Quan hệ Owner–Manager hiện tại","branches":"Cơ sở và Manager hiện tại, QR / Zalo","branch_policies":"Phiên bản chính sách Branch","room_types":"Loại phòng trong Branch","rooms":"Phòng / chế độ cho thuê / thông tin tìm kiếm","room_slots":"Slot thuộc Room","appointments":"Lịch xem theo khoảng giờ","bookings":"Yêu cầu giữ Room/Slot của User","contracts":"Hợp đồng chính của Room","contract_tenants":"Thành viên, thỏa thuận cá nhân, Booking nguồn","resource_allocations":"Khóa tài nguyên giữ/thuê; tách cư trú","room_slot_occupancies":"Khoảng cư trú thực tế","contract_amendments":"Phụ lục / lịch sử thay đổi thỏa thuận","services":"Danh mục dịch vụ theo Branch","room_services":"Lịch sử cấu hình và giá dịch vụ phòng","contract_services":"Dịch vụ / giá thỏa thuận Contract hoặc Member","room_service_charges":"Phí theo segment, chỉ số, giá snapshot","cost_allocation_proposals":"Đề xuất và phân bổ được duyệt bằng JSONB","invoices":"Hóa đơn riêng ContractTenant/tháng","invoice_items":"Dòng phí và dòng áp dụng tiền trả trước","payments":"Khoản nhận tiền và phân loại / hoàn tiền","payment_webhook_events":"Bằng chứng xử lý callback và chống lặp","furnitures":"Tài sản phòng","furniture_warranties":"Bảo hành tài sản","report_requests":"Yêu cầu sửa chữa","report_requests_services":"Dịch vụ bị báo hỏng","report_requests_furnitures":"Tài sản bị báo hỏng","report_records":"Lịch sử xử lý / phân công / COST / LIABILITY","notifications":"Thông báo trong ứng dụng","threads":"Thread tìm phòng","audit_logs":"Audit chỉ được thêm"};
Object.assign(purpose,{users:'Hồ sơ cá nhân và tài khoản chung; auth/Owner Verification tối giản',contract_tenants:'Nghĩa vụ User theo Contract, snapshot lúc ký, move-out/handover',resource_allocations:'Giữ/thuê tài nguyên hoặc chặn bảo trì theo khoảng ngày',contracts:'Hợp đồng chính của Room và PDF/evidence chính',contract_amendments:'Phụ lục đã duyệt và phiên bản tài liệu bổ sung',notifications:'Thông báo theo người nhận/kênh; delivery/retry tối thiểu',threads:'Thread cơ bản; không Comment/Suggestion/Report'});
const old=new Set('branches room_types rooms room_slots appointments bookings contracts contract_tenants room_slot_occupancies services room_services room_service_charges contract_services invoices invoice_items payments furnitures furniture_warranties report_requests report_requests_services report_requests_furnitures report_records'.split(' '));
const phase2=new Set(['contract_amendments']);
const phase3=new Set(['threads']);
const phase=t=>phase3.has(t)?3:phase2.has(t)?2:1;
const owner=t=>Object.entries(groups).find(([,names])=>names.includes(t))?.[0];
const cols=t=>db.columns.filter(c=>c.table_name===t),cons=t=>db.constraints.filter(c=>c.table_name===t),colNames=(t,ns)=>ns.map(n=>cols(t)[n-1].column_name);
if(db.tables.some(t=>!owner(t.name)) || Object.values(groups).flat().length!==db.tables.length) throw Error('Unmapped table');
let md='# Catalog PostgreSQL rental — thiết kế 34 bảng\n\nSinh bằng `generate-docs.mjs` từ `schema-catalog.json`; không ghi dữ liệu người dùng. Phase 1: 32 bảng; Phase 2: thêm 1; Phase 3: thêm 1. Phase 2/3 có ALTER TABLE cho các cột gộp; catalog hiển thị cấu hình đầy đủ.\n\n';
md+='## Danh sách bảng và module sở hữu ghi\n\n| Table | Mục đích | PK | FK quan trọng | Phase | Thay đổi từ ERD cũ | Module ghi |\n|---|---|---|---|---|---|---|\n';
for(const {name:t} of db.tables){const cs=cons(t);md+=`| \`${t}\` | ${purpose[t]} | ${cs.filter(c=>c.type==='p').map(c=>c.definition).join(';')} | ${[...new Set(cs.filter(c=>c.type==='f').map(c=>c.referenced_table))].join(', ')||'—'} | ${phase(t)} | ${old.has(t)?'Sửa cấu trúc / ràng buộc':'Thêm'} | ${owner(t)} |\n`;}
md+='\n`MANAGERS` được gộp vào `users` + role `MANAGER` + `owner_managers`; `APPOINTMENT` chuẩn hóa thành `appointments`. Không tạo `tenant_contracts`, `payment_allocations`, `cost_allocation_items`, `bank_transactions`, `owner_bank_accounts`, `payment_requests`, `file_metadata`, bảng meter, deposit ledger hay dashboard. Không có bảng cũ nào được sao chép nguyên DDL: giữ khái niệm nhưng chuẩn hóa PK/FK/type/scope.\n\n`reporting` chỉ đọc bảng/view qua API module hoặc read query có kiểm tra phạm vi; không sở hữu bảng nghiệp vụ. `common.audit` là hạ tầng audit, không thêm module `platform`. `identity` sở hữu hồ sơ tenant; `leasing` sở hữu nghĩa vụ thuê và cư trú.\n';
for(const {name:t} of db.tables){md+=`\n## ${t} — Phase ${phase(t)}\n\n${purpose[t]}. Module: \`${owner(t)}\`.\n\n| Cột | Kiểu | NULL | Default |\n|---|---|---|---|\n`;for(const c of cols(t))md+=`| ${c.column_name} | ${c.data_type} | ${c.nullable?'Có':'Không'} | ${c.default_value??'—'} |\n`;md+='\n```sql\n'+cons(t).map(c=>`${c.name}: ${c.definition}`).join('\n')+'\n```\n\nIndex (bao gồm index do PK/UNIQUE tự tạo):\n\n```sql\n'+db.indexes.filter(i=>i.table_name===t).map(i=>i.definition+';').join('\n')+'\n```\n';}
fs.writeFileSync(path.join(dir,'TABLE_CATALOG.md'),md);
const esc=s=>String(s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
const colors=['#dae8fc','#d5e8d4','#fff2cc'];
function card(t,detail){const cs=cons(t);let fields=cols(t).map((c,i)=>({c,i:i+1}));if(!detail) fields=fields.filter(({c,i})=>cs.some(k=>['p','f'].includes(k.type)&&k.column_numbers.includes(i))||['status','document_status','rental_mode'].includes(c.column_name));let lines=fields.map(({c,i})=>{const keys=cs.filter(k=>['p','f','u'].includes(k.type)&&k.column_numbers.includes(i)).map(k=>({p:'PK',f:'FK',u:'UQ'}[k.type]));return esc(`${[...new Set(keys)].join('/')} ${c.column_name}${detail?' : '+c.data_type:''}${c.nullable?' ?':' !'}`)});return {html:`<b>${t} · P${phase(t)}</b><br/><i>${owner(t)}</i><hr/>`+lines.join('<br/>'),height:70+lines.length*20};}
function page(id,title,names,detail,refs=true){let cells=['<mxCell id="0"/>','<mxCell id="1" parent="0"/>'];let objects=names.map(t=>({t,...card(t,detail)}));let rowY=100;let positions=new Map();for(let i=0;i<objects.length;i+=3){const row=objects.slice(i,i+3);row.forEach((o,j)=>positions.set(o.t,{x:40+j*780,y:rowY,w:510,h:o.height}));rowY+=Math.max(...row.map(o=>o.height))+220;}
let external=refs?[...new Set(db.constraints.filter(c=>names.includes(c.table_name)&&c.type==='f'&&!names.includes(c.referenced_table)).map(c=>c.referenced_table))]:[];
external.forEach((t,i)=>positions.set(t,{x:40+(i%3)*780,y:rowY+Math.floor(i/3)*150,w:510,h:65}));
cells.push(`<mxCell id="legend" value="${esc(title+' | ! NOT NULL, ? nullable; edges child -> parent; see TABLE_CATALOG.md for all CHECK/partial UNIQUE definitions. Cross-module FKs do not authorize cross-module writes.')}" style="text;html=0;whiteSpace=wrap;fontSize=16;align=left;" vertex="1" parent="1"><mxGeometry x="40" y="10" width="2050" height="65" as="geometry"/></mxCell>`);
for(const [t,p] of positions){const o=objects.find(o=>o.t===t);const val=o?.html??`<b>${t} · P${phase(t)}</b><br/>External reference: ${owner(t)}`;cells.push(`<mxCell id="t-${t}" value="${esc(val)}" style="rounded=0;whiteSpace=wrap;html=1;align=left;verticalAlign=top;spacing=10;fontSize=13;fillColor=${o?colors[phase(t)-1]:'#f5f5f5'};strokeColor=#666666;" vertex="1" parent="1"><mxGeometry x="${p.x}" y="${p.y}" width="${p.w}" height="${p.h}" as="geometry"/></mxCell>`);}
for(const c of db.constraints.filter(c=>c.type==='f'&&names.includes(c.table_name)&&positions.has(c.referenced_table))){const cn=colNames(c.table_name,c.column_numbers),rn=colNames(c.referenced_table,c.referenced_numbers);const optional=c.column_numbers.some(n=>cols(c.table_name)[n-1].nullable);const unique=cons(c.table_name).some(u=>['p','u'].includes(u.type)&&u.column_numbers.every(n=>c.column_numbers.includes(n)));const label=`${cn.join(', ')} → ${rn.join(', ')}\nchild 0..${unique?'1':'N'} : parent ${optional?'0..1':'1'}`;cells.push(`<mxCell id="fk-${c.name}-${c.table_name}" value="${esc(label)}" style="edgeStyle=orthogonalEdgeStyle;rounded=0;html=0;endArrow=${optional?'ERzeroToOne':'ERone'};startArrow=${unique?'ERzeroToOne':'ERzeroToMany'};fontSize=10;labelBackgroundColor=#ffffff;strokeColor=#888888;" edge="1" parent="1" source="t-${c.table_name}" target="t-${c.referenced_table}"><mxGeometry relative="1" as="geometry"/></mxCell>`);}
return `  <diagram id="rental-${id}" name="${esc(title)}"><mxGraphModel grid="1" gridSize="10" page="0" pageWidth="2400" pageHeight="${rowY+Math.ceil(external.length/3)*150+100}"><root>${cells.join('\n')}</root></mxGraphModel></diagram>\n`;}
const file=path.join(docs,'All_Diagrams.drawio');let xml=fs.readFileSync(file,'utf8');xml=xml.replace(/\s*<diagram id="rental-[\s\S]*?<\/diagram>/g,'');
for(const name of ['Conceptual ERD','Logical ERD','Logical ERD_Ver2'])xml=xml.replace(`name="${name}"`,`name="Legacy/Deprecated — ${name}"`);
xml=xml.replace(/name="Legacy [^"]*"/g,m=>m.replace('Legacy ','Legacy/Deprecated '));
const core=['users','roles','user_roles','owner_managers','branches','rooms','room_slots','bookings','contracts','contract_tenants','resource_allocations','room_slot_occupancies','room_service_charges','cost_allocation_proposals','invoices','invoice_items','payments','report_records','notifications','threads'];
let pages=page('conceptual','Conceptual ERD',core,false,false)+page('logical','Logical ERD_Ver2',Object.values(groups).flat(),false,false);
for(const [module,names] of Object.entries(groups))pages+=page('physical-'+module,'Physical — '+module,names,true);
xml=xml.replace('</mxfile>',pages+'</mxfile>');xml=xml.replace(/pages="\d+"/,`pages="${(xml.match(/<diagram /g)||[]).length}"`);fs.writeFileSync(file,xml);
fs.writeFileSync(path.join(dir,'module-table-map.json'),JSON.stringify(groups,null,2)+'\n');
console.log(`Generated catalog for ${db.tables.length} tables and ${Object.keys(groups).length} physical ERD pages; legacy pages preserved.`);
