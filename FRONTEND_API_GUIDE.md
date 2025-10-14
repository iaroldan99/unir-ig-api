# 📱 Guía de Integración Frontend - API Gateway

Esta guía explica cómo consumir la API desde tu frontend (React, Vue, Angular, etc.)

## 🔑 Base URL

```
Local: http://localhost:8080
Producción: https://tudominio.com
```

## 📋 Tabla de Contenidos

1. [Autenticación Instagram](#1-autenticación-instagram)
2. [Gestión de Threads (Conversaciones)](#2-gestión-de-threads)
3. [Gestión de Mensajes](#3-gestión-de-mensajes)
4. [Stream en Tiempo Real (SSE)](#4-stream-en-tiempo-real)

---

## 1. Autenticación Instagram

### 1.1 Conectar Cuenta de Instagram Business

**Flow completo:**

```javascript
// Paso 1: Usuario hace clic en "Conectar Instagram"
const connectInstagram = async (userId) => {
  // Opción A: Redirigir directamente
  window.location.href = `http://localhost:8080/auth/instagram/connect?userId=${userId}`;
  
  // Opción B: Obtener URL y abrir en ventana nueva
  const response = await fetch(
    `http://localhost:8080/auth/instagram/authorization-url?userId=${userId}`
  );
  const { authorizationUrl } = await response.json();
  window.open(authorizationUrl, '_blank', 'width=600,height=700');
};
```

**Callback después de autorizar:**

Instagram redirigirá a: `http://localhost:8080/auth/instagram/callback?code=...`

El backend procesará y luego redirigirá al frontend a:
```
http://localhost:3000/settings/accounts?connected=instagram&accountId=uuid
```

Tu frontend debe detectar esto:

```javascript
// En tu página de settings
useEffect(() => {
  const params = new URLSearchParams(window.location.search);
  
  if (params.get('connected') === 'instagram') {
    const accountId = params.get('accountId');
    showSuccessMessage(`Instagram conectado! Account ID: ${accountId}`);
    refreshAccountStatus();
  }
  
  if (params.get('error')) {
    showErrorMessage('Error conectando Instagram');
  }
}, []);
```

### 1.2 Verificar Estado de Conexión

```http
GET /auth/instagram/status?userId=<uuid>
```

**Ejemplo React:**

```jsx
const [instagramStatus, setInstagramStatus] = useState(null);

useEffect(() => {
  const checkInstagramStatus = async () => {
    const response = await fetch(
      `http://localhost:8080/auth/instagram/status?userId=${currentUserId}`
    );
    const data = await response.json();
    setInstagramStatus(data);
  };
  
  checkInstagramStatus();
}, [currentUserId]);

// Usar en el UI
{instagramStatus?.connected ? (
  <div>
    <p>✅ Instagram conectado: @{instagramStatus.username}</p>
    <button onClick={disconnectInstagram}>Desconectar</button>
  </div>
) : (
  <button onClick={() => connectInstagram(currentUserId)}>
    Conectar Instagram
  </button>
)}
```

**Response:**

```json
{
  "connected": true,
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "displayName": "Mi Tienda",
  "username": "mitienda_oficial",
  "instagramUserId": "17841405793187218",
  "connectedAt": "2025-01-14T10:30:00Z"
}
```

### 1.3 Desconectar Instagram

```http
POST /auth/instagram/disconnect?userId=<uuid>
```

**Ejemplo:**

```javascript
const disconnectInstagram = async () => {
  const response = await fetch(
    `http://localhost:8080/auth/instagram/disconnect?userId=${currentUserId}`,
    { method: 'POST' }
  );
  
  if (response.ok) {
    showSuccessMessage('Instagram desconectado');
    refreshAccountStatus();
  }
};
```

---

## 2. Gestión de Threads (Conversaciones)

### 2.1 Listar Todos los Threads

```http
GET /v1/threads?page=0&size=20&channel=instagram&accountId=<uuid>
```

**Parámetros query:**
- `page` - Número de página (default: 0)
- `size` - Tamaños de página (default: 20)
- `channel` - Filtrar por canal: `instagram`, `whatsapp`, `gmail`
- `accountId` - Filtrar por cuenta específica
- `q` - Búsqueda de texto

**Ejemplo React:**

```jsx
const [threads, setThreads] = useState([]);
const [loading, setLoading] = useState(false);

const loadThreads = async (page = 0) => {
  setLoading(true);
  try {
    const response = await fetch(
      `http://localhost:8080/v1/threads?page=${page}&size=20`
    );
    const data = await response.json();
    setThreads(data.content);
  } catch (error) {
    console.error('Error loading threads:', error);
  } finally {
    setLoading(false);
  }
};

useEffect(() => {
  loadThreads();
}, []);
```

**Response:**

```json
{
  "content": [
    {
      "id": "thread-uuid",
      "accountId": "account-uuid",
      "channel": "INSTAGRAM",
      "externalThreadId": "1234567890",
      "participants": [
        {
          "id": "1234567890",
          "name": "Cliente Juan"
        }
      ],
      "subject": null,
      "lastMessageAt": "2025-01-14T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 45,
  "totalPages": 3
}
```

### 2.2 Obtener Thread Individual

```http
GET /v1/threads/{threadId}
```

**Ejemplo:**

```javascript
const getThread = async (threadId) => {
  const response = await fetch(
    `http://localhost:8080/v1/threads/${threadId}`
  );
  return await response.json();
};
```

---

## 3. Gestión de Mensajes

### 3.1 Listar Mensajes de un Thread

```http
GET /v1/threads/{threadId}/messages?page=0&size=50
```

**Ejemplo React:**

```jsx
const [messages, setMessages] = useState([]);

const loadMessages = async (threadId) => {
  const response = await fetch(
    `http://localhost:8080/v1/threads/${threadId}/messages?size=50`
  );
  const data = await response.json();
  setMessages(data.content);
};
```

**Response:**

```json
{
  "content": [
    {
      "id": "message-uuid",
      "threadId": "thread-uuid",
      "channel": "INSTAGRAM",
      "direction": "INBOUND",
      "externalMessageId": "mid.ABC123",
      "sender": {
        "id": "1234567890",
        "name": "Cliente Juan"
      },
      "recipients": [
        {
          "id": "17841...",
          "name": "Me"
        }
      ],
      "bodyText": "Hola! Quiero hacer una consulta",
      "bodyHtml": null,
      "attachments": [],
      "status": "received",
      "createdAt": "2025-01-14T10:30:00Z"
    }
  ]
}
```

### 3.2 Enviar Mensaje

```http
POST /v1/messages
Content-Type: application/json
```

**Body:**

```json
{
  "channel": "INSTAGRAM",
  "accountId": "account-uuid",
  "threadId": "thread-uuid-optional",
  "to": [
    {
      "id": "1234567890"
    }
  ],
  "text": "Gracias por tu mensaje! ¿En qué puedo ayudarte?",
  "html": null,
  "attachments": []
}
```

**Ejemplo React:**

```jsx
const sendMessage = async (threadId, accountId, recipientId, text) => {
  const response = await fetch('http://localhost:8080/v1/messages', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      channel: 'INSTAGRAM',
      accountId: accountId,
      threadId: threadId,
      to: [{ id: recipientId }],
      text: text
    })
  });
  
  if (!response.ok) {
    throw new Error('Error enviando mensaje');
  }
  
  return await response.json();
};

// En tu componente de chat
const handleSendMessage = async () => {
  try {
    await sendMessage(
      currentThread.id,
      currentThread.accountId,
      currentThread.participants[0].id,
      messageText
    );
    
    setMessageText('');
    loadMessages(currentThread.id); // Recargar mensajes
  } catch (error) {
    showErrorMessage('Error enviando mensaje');
  }
};
```

---

## 4. Stream en Tiempo Real (SSE)

Para recibir notificaciones en tiempo real de nuevos mensajes.

```http
GET /v1/stream
Accept: text/event-stream
```

**Ejemplo con EventSource (navegador nativo):**

```javascript
const connectToStream = () => {
  const eventSource = new EventSource('http://localhost:8080/v1/stream');
  
  // Evento: nuevo mensaje
  eventSource.addEventListener('message.created', (event) => {
    const message = JSON.parse(event.data);
    console.log('Nuevo mensaje:', message);
    
    // Actualizar UI
    addMessageToThread(message.threadId, message);
    playNotificationSound();
  });
  
  // Evento: thread actualizado
  eventSource.addEventListener('thread.updated', (event) => {
    const thread = JSON.parse(event.data);
    console.log('Thread actualizado:', thread);
    
    // Actualizar lista de threads
    updateThreadInList(thread);
  });
  
  // Errores
  eventSource.onerror = (error) => {
    console.error('Error en stream:', error);
    eventSource.close();
    
    // Reconectar después de 5 segundos
    setTimeout(connectToStream, 5000);
  };
  
  return eventSource;
};

// En tu componente principal
useEffect(() => {
  const eventSource = connectToStream();
  
  return () => {
    eventSource.close();
  };
}, []);
```

**Ejemplo con librería (react-use-sse):**

```bash
npm install react-use-sse
```

```jsx
import { useSSE } from 'react-use-sse';

function InboxApp() {
  const sse = useSSE('http://localhost:8080/v1/stream', {
    initialState: null,
  });
  
  useEffect(() => {
    if (sse.data) {
      const event = JSON.parse(sse.data);
      
      if (event.type === 'message.created') {
        handleNewMessage(event.data);
      }
      
      if (event.type === 'thread.updated') {
        handleThreadUpdate(event.data);
      }
    }
  }, [sse.data]);
  
  return (
    <div>
      {sse.error && <div>Error conectando al stream</div>}
      {/* Tu UI */}
    </div>
  );
}
```

---

## 5. Health Check

```http
GET /health
```

Útil para verificar que el servicio está disponible.

```javascript
const checkAPIHealth = async () => {
  try {
    const response = await fetch('http://localhost:8080/health');
    const data = await response.json();
    return data.status === 'UP';
  } catch (error) {
    return false;
  }
};
```

---

## 📦 Ejemplo Completo: Componente de Inbox

```jsx
import React, { useState, useEffect } from 'react';

function InboxApp() {
  const currentUserId = 'tu-user-uuid'; // Obtener del contexto de autenticación
  const [threads, setThreads] = useState([]);
  const [selectedThread, setSelectedThread] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState('');
  const [instagramConnected, setInstagramConnected] = useState(false);

  // Verificar conexión de Instagram
  useEffect(() => {
    const checkInstagram = async () => {
      const response = await fetch(
        `http://localhost:8080/auth/instagram/status?userId=${currentUserId}`
      );
      const data = await response.json();
      setInstagramConnected(data.connected);
    };
    checkInstagram();
  }, []);

  // Cargar threads
  useEffect(() => {
    const loadThreads = async () => {
      const response = await fetch('http://localhost:8080/v1/threads');
      const data = await response.json();
      setThreads(data.content);
    };
    loadThreads();
  }, []);

  // Cargar mensajes cuando se selecciona un thread
  useEffect(() => {
    if (selectedThread) {
      const loadMessages = async () => {
        const response = await fetch(
          `http://localhost:8080/v1/threads/${selectedThread.id}/messages`
        );
        const data = await response.json();
        setMessages(data.content);
      };
      loadMessages();
    }
  }, [selectedThread]);

  // SSE para tiempo real
  useEffect(() => {
    const eventSource = new EventSource('http://localhost:8080/v1/stream');
    
    eventSource.addEventListener('message.created', (event) => {
      const message = JSON.parse(event.data);
      if (selectedThread && message.threadId === selectedThread.id) {
        setMessages(prev => [...prev, message]);
      }
    });
    
    return () => eventSource.close();
  }, [selectedThread]);

  // Enviar mensaje
  const handleSendMessage = async () => {
    if (!messageText.trim() || !selectedThread) return;
    
    await fetch('http://localhost:8080/v1/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        channel: 'INSTAGRAM',
        accountId: selectedThread.accountId,
        threadId: selectedThread.id,
        to: [{ id: selectedThread.participants[0].id }],
        text: messageText
      })
    });
    
    setMessageText('');
  };

  return (
    <div className="inbox-app">
      {!instagramConnected && (
        <div className="banner">
          <p>Instagram no conectado</p>
          <button onClick={() => window.location.href = 
            `http://localhost:8080/auth/instagram/connect?userId=${currentUserId}`
          }>
            Conectar Instagram
          </button>
        </div>
      )}
      
      <div className="inbox-container">
        {/* Lista de threads */}
        <div className="threads-list">
          {threads.map(thread => (
            <div 
              key={thread.id}
              onClick={() => setSelectedThread(thread)}
              className={selectedThread?.id === thread.id ? 'active' : ''}
            >
              <h4>{thread.participants[0]?.name || 'Cliente'}</h4>
              <p>{new Date(thread.lastMessageAt).toLocaleString()}</p>
            </div>
          ))}
        </div>
        
        {/* Mensajes */}
        <div className="messages-container">
          {selectedThread && (
            <>
              <div className="messages-list">
                {messages.map(msg => (
                  <div 
                    key={msg.id}
                    className={`message ${msg.direction.toLowerCase()}`}
                  >
                    <p>{msg.bodyText}</p>
                    <span>{new Date(msg.createdAt).toLocaleString()}</span>
                  </div>
                ))}
              </div>
              
              <div className="message-input">
                <input
                  value={messageText}
                  onChange={(e) => setMessageText(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                  placeholder="Escribe un mensaje..."
                />
                <button onClick={handleSendMessage}>Enviar</button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default InboxApp;
```

---

## 🔐 Variables de Entorno Frontend

Crea un archivo `.env` en tu frontend:

```env
REACT_APP_API_URL=http://localhost:8080
REACT_APP_FRONTEND_URL=http://localhost:3000
```

Y úsalo:

```javascript
const API_URL = process.env.REACT_APP_API_URL;

const response = await fetch(`${API_URL}/v1/threads`);
```

---

## 🚀 Checklist de Integración

- [ ] Usuario puede conectar Instagram Business
- [ ] Se muestra estado de conexión (conectado/desconectado)
- [ ] Lista de threads se carga correctamente
- [ ] Al hacer clic en un thread, se cargan sus mensajes
- [ ] Se pueden enviar mensajes
- [ ] SSE muestra nuevos mensajes en tiempo real
- [ ] Notificaciones de nuevos mensajes
- [ ] Manejo de errores (API caída, sin conexión, etc.)
- [ ] Loading states mientras cargan datos
- [ ] Paginación de threads y mensajes

---

## 📚 Siguientes Pasos

1. Implementar autenticación de usuarios (JWT)
2. Agregar WhatsApp Business
3. Agregar Gmail
4. Implementar búsqueda de mensajes
5. Filtros por canal/fecha
6. Exportar conversaciones
7. Asignación de conversaciones a agentes
8. Tags y categorías

---

¿Necesitas ayuda con algo específico del frontend? Por ejemplo:
- Ejemplos con Vue o Angular
- Configuración de CORS
- Manejo de autenticación
- WebSockets en lugar de SSE

