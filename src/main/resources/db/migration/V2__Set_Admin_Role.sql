-- DataInitializer creaba el usuario "admin" sin asignar rol, así que quedó con el DEFAULT 'USER'.
-- Solo corrige el admin activo: si no existe (BD nueva), DataInitializer ya lo crea con rol ADMIN.
UPDATE USERS
SET rol = 'ADMIN'
WHERE username = 'admin'
  AND status <> 'DELETED';
