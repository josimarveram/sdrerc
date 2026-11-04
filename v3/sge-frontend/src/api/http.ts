const SESSION_STORAGE_KEY = 'sdrerc.v3.session'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/** Token de sesion actual, leido directo de sessionStorage (ver auth/AuthContext.tsx). */
function currentAccessToken(): string | null {
  const raw = sessionStorage.getItem(SESSION_STORAGE_KEY)
  if (!raw) return null
  try {
    return (JSON.parse(raw) as { accessToken?: string }).accessToken ?? null
  } catch {
    return null
  }
}

/**
 * 01/09/2026: causa raíz real de "Ocurrió un error inesperado." apareciendo a los pocos minutos
 * de iniciar sesión — el JWT de sesión dura 10 minutos (`sdrerc.v3.jwt.session-ttl-minutes`,
 * bajado de 30 el 01/09/2026 junto con `InactivityGuard.tsx`) y,
 * al vencer, cualquier request quedaba sin manejo específico: se mostraba el error genérico en
 * la página donde el usuario estuviera, sin indicar que debía volver a iniciar sesión. Ahora se
 * limpia la sesión guardada y se redirige a /login con un aviso, en vez de dejar la página
 * mostrando un error sin salida. El backend (`JsonAuthenticationEntryPoint`, ver v3/CLAUDE.md)
 * ya devuelve 401 con un mensaje real en este caso — antes devolvía 403 con cuerpo vacío.
 */
function manejarSesionVencida() {
  try {
    sessionStorage.removeItem(SESSION_STORAGE_KEY)
  } catch {
    // sessionStorage no disponible (modo privado extremo); igual se intenta redirigir.
  }
  if (!window.location.pathname.startsWith('/login')) {
    window.location.assign('/login?sesionVencida=1')
  }
}

async function handleResponse<TResponse>(response: Response): Promise<TResponse> {
  const text = await response.text()
  const data = text ? JSON.parse(text) : null
  if (!response.ok) {
    if (response.status === 401) manejarSesionVencida()
    const message = data && typeof data.message === 'string' ? data.message : 'Ocurrió un error inesperado.'
    throw new ApiError(message, response.status)
  }
  return data as TResponse
}

/**
 * Wrapper minimo sobre fetch para /api/*. Todas las respuestas de error del backend V3
 * (ver GlobalExceptionHandler) tienen la forma { message: string } - se propaga ese mensaje
 * tal cual, son los mismos mensajes ya validados en V2 (genericos para credenciales, por
 * diseño anti-enumeracion de cuentas).
 */
export async function apiPost<TResponse>(path: string, body: unknown): Promise<TResponse> {
  const token = currentAccessToken()
  const response = await fetch(path, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  })
  return handleResponse<TResponse>(response)
}

/** Igual que apiPost, pero PUT (ej. guardar cambios de Edición manual). */
export async function apiPut<TResponse>(path: string, body: unknown, params?: Record<string, string>): Promise<TResponse> {
  const token = currentAccessToken()
  const query = params ? `?${new URLSearchParams(params).toString()}` : ''
  const response = await fetch(`${path}${query}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  })
  return handleResponse<TResponse>(response)
}

/** Igual que apiPost, para endpoints protegidos de solo lectura (ej. /api/dashboard). */
export async function apiGet<TResponse>(path: string, params?: Record<string, string>): Promise<TResponse> {
  const token = currentAccessToken()
  const query = params ? `?${new URLSearchParams(params).toString()}` : ''
  const response = await fetch(`${path}${query}`, {
    method: 'GET',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  return handleResponse<TResponse>(response)
}

/** Igual que apiPost, sin body (ej. retirar un integrante de un grupo familiar). */
export async function apiDelete<TResponse>(path: string): Promise<TResponse> {
  const token = currentAccessToken()
  const response = await fetch(path, {
    method: 'DELETE',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  return handleResponse<TResponse>(response)
}

/** Igual que apiPost, pero para multipart/form-data (subida de archivo, ej. Carga diaria). */
export async function apiPostForm<TResponse>(path: string, form: FormData): Promise<TResponse> {
  const token = currentAccessToken()
  const response = await fetch(path, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form,
  })
  return handleResponse<TResponse>(response)
}

async function handleBlobResponse(response: Response): Promise<Blob> {
  if (!response.ok) {
    if (response.status === 401) manejarSesionVencida()
    const text = await response.text()
    let message = 'Ocurrió un error inesperado.'
    try {
      const data = text ? JSON.parse(text) : null
      if (data && typeof data.message === 'string') message = data.message
    } catch {
      // El cuerpo de error no era JSON; se usa el mensaje genérico.
    }
    throw new ApiError(message, response.status)
  }
  return response.blob()
}

/** Descarga binaria autenticada (ej. exportar previsualización de Carga diaria a Excel). */
export async function apiPostBlob(path: string, body: unknown): Promise<Blob> {
  const token = currentAccessToken()
  const response = await fetch(path, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
  })
  return handleBlobResponse(response)
}

/** Igual que apiPostBlob, sin body (ej. descargar la plantilla oficial de Carga diaria). */
export async function apiGetBlob(path: string): Promise<Blob> {
  const token = currentAccessToken()
  const response = await fetch(path, {
    method: 'GET',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  return handleBlobResponse(response)
}

/** Dispara la descarga de un Blob ya obtenido, con el nombre de archivo indicado. */
export function descargarBlob(blob: Blob, nombreArchivo: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = nombreArchivo
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
