export const PERMISSIONS = [
  { value: 'CREATE', label: 'Create', description: 'Add new employees and attach files' },
  { value: 'READ', label: 'Read', description: 'View employees, photos, CVs and contracts' },
  { value: 'UPDATE', label: 'Update', description: 'Edit employees and replace files' },
  { value: 'DELETE', label: 'Delete', description: 'Remove employees' },
]

export const ROLES = [
  { value: 'USER', label: 'Normal User' },
  { value: 'SUPER_ADMIN', label: 'Super Administrator' },
]

export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
