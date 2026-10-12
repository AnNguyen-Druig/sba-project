// Run with Node; requires @electric-sql/pglite installed in be-sba-project/target/rental-ddl-check.
// Real PostgreSQL engine in WASM; NOT a Supabase connection or an HTTP authorization test.
import { createRequire } from 'node:module';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const dir=path.dirname(fileURLToPath(import.meta.url));
const root=path.resolve(dir,'../../..');
const require=createRequire(path.join(root,'be-sba-project/target/rental-ddl-check/package.json'));
const {PGlite}=require('@electric-sql/pglite');
const db=new PGlite();
let stage='';
const phase=Number(process.argv.find(a=>a.startsWith('--phase='))?.split('=')[1]??3);
if(![1,2,3].includes(phase))throw Error('Use --phase=1, 2 or 3');
try {
 for(const file of ['01_phase1.sql','02_phase1_integrity.sql','03_phase2.sql','04_phase3.sql','09_access.sql','05_seed_roles.sql','06_seed_demo.sql','07_verify.sql','08_negative_tests.sql']) {
  stage=file;
  if((file==='03_phase2.sql'&&phase<2)||(file==='04_phase3.sql'&&phase<3))continue;
  const results=await db.exec(fs.readFileSync(path.join(dir,file),'utf8'));
  if(file==='08_negative_tests.sql'){
   const cases=results.flatMap(r=>r.rows??[]).filter(r=>r.label&&r.result);
   const expectedCases={1:45,2:48,3:50}[phase];
   if(cases.length!==expectedCases||cases.some(r=>r.result!=='PASS'))throw Error('Negative integrity cases missing or failed');
   process.stdout.write(`PASS ${cases.length} negative integrity cases\n`);
  }
  process.stdout.write(`PASS ${file}\n`);
  if(['02_phase1_integrity.sql','03_phase2.sql','04_phase3.sql'].includes(file)){
   const expected={'02_phase1_integrity.sql':32,'03_phase2.sql':33,'04_phase3.sql':34}[file];
   const actual=(await db.query("select count(*)::int n from pg_tables where schemaname='rental'")).rows[0].n;
   if(actual!==expected)throw Error(`Expected ${expected} tables, got ${actual}`);
   process.stdout.write(`PASS ${expected} phase tables\n`);
  }
 }
 const result=await db.query("select count(*)::int tables from pg_tables where schemaname='rental'");
 process.stdout.write(JSON.stringify(result.rows)+'\n');
 const catalog={};
 catalog.tables=(await db.query("select tablename name from pg_tables where schemaname='rental' order by tablename")).rows;
 catalog.columns=(await db.query("select c.relname table_name,a.attname column_name,format_type(a.atttypid,a.atttypmod) data_type,not a.attnotnull nullable,pg_get_expr(d.adbin,d.adrelid) default_value from pg_attribute a join pg_class c on c.oid=a.attrelid join pg_namespace n on n.oid=c.relnamespace left join pg_attrdef d on d.adrelid=a.attrelid and d.adnum=a.attnum where n.nspname='rental' and c.relkind='r' and a.attnum>0 and not a.attisdropped order by c.relname,a.attnum")).rows;
 catalog.constraints=(await db.query("select c.relname table_name,co.conname name,co.contype type,pg_get_constraintdef(co.oid) definition,tc.relname referenced_table,co.conkey column_numbers,co.confkey referenced_numbers from pg_constraint co join pg_class c on c.oid=co.conrelid left join pg_class tc on tc.oid=co.confrelid where co.connamespace='rental'::regnamespace order by c.relname,co.conname")).rows;
 catalog.indexes=(await db.query("select tablename table_name,indexname name,indexdef definition from pg_indexes where schemaname='rental' order by tablename,indexname")).rows;
 const removed='owner_verifications tenants verification_challenges login_attempts refresh_sessions handover_records move_out_requests room_unavailability_periods notification_preferences contract_documents document_signatures moderation_reports notification_outbox notification_templates thread_comments thread_room_suggestions'.split(' ');
 if(catalog.tables.some(t=>removed.includes(t.name))||catalog.constraints.some(c=>removed.includes(c.referenced_table)))throw Error('Removed table/FK remains');
 const routines=(await db.query("select prosrc definition from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='rental' union all select definition from pg_views where schemaname='rental'")).rows;
 const obsoleteReference=new RegExp('\\b(?:from|join|update|into|references|on)\\s+(?:rental\\.)?"?(?:'+removed.join('|')+')"?\\b','i');
 if(routines.some(r=>obsoleteReference.test(r.definition)))throw Error('Routine/view references removed relation');
 process.stdout.write('PASS no removed tables, foreign keys or routine/view relation references\n');
 if(process.argv.includes('--write-catalog')&&phase===3) fs.writeFileSync(path.join(dir,'schema-catalog.json'),JSON.stringify(catalog,null,2)+'\n');
 process.stdout.write(`PostgreSQL: ${(await db.query('select version()')).rows[0].version}\n`);
} catch(error) {
 process.stderr.write(`FAIL ${stage}: ${error.message}\n${error.where??''}\n${error.detail??''}\n`);
 process.exitCode=1;
} finally {await db.close();}
