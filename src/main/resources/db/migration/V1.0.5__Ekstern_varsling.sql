alter table varsel add column eksternVarsling boolean;

update varsel set eksternVarsling = true where eksternVarsling is null;
