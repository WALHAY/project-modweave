export function normalizeId(value: unknown): string {
  if (value === null || value === undefined) {
    return ''
  }
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    return String(value)
  }
  if (typeof value === 'object') {
    const record = value as Record<string, unknown>
    if (record.value !== undefined) {
      return normalizeId(record.value)
    }
    if (record.id !== undefined) {
      return normalizeId(record.id)
    }
    if (record.name !== undefined) {
      return normalizeId(record.name)
    }
    if (record.key !== undefined) {
      return normalizeId(record.key)
    }
  }
  return ''
}

export function normalizeKey(value: unknown): string {
  return normalizeId(value)
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '')
}
