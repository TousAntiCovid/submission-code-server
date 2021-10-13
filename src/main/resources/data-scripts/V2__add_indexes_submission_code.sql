create index if not exists type_code_idx on submission_code(type_code);
create index if not exists type_code_date_available_idx on submission_code(type_code, date_available);
