export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/**
 * Wrapper minimo sobre fetch para /api/*. Todas las respuestas de error del backend V3
 * (ver GlobalExceptionHandler) tienen la forma { message: string } - se propaga ese mensaje
 * tal cual, son los mismos mensajes ya validados en V2 (genericos para credenciales, por
 * diseño anti-enumeracion de cuentas).
 */
export async function apiPost<TResponse>(path: string, body: unknown): Promise<TResponse> {
  const response = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const text = await response.text()
  const data = text ? JSON.parse(text) : null
  if (!response.ok) {
    const message = data && typeof data.message === 'string' ? data.message : 'Ocurrió un error inesperado.'
    throw new ApiError(message, response.status)
  }
  return data as TResponse
}
