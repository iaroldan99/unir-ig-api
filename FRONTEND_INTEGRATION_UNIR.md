# 🎨 Integración Frontend "Unir" con Backend

## ✅ Estado Actual: Backend LISTO

Tu backend está 100% funcional y listo para conectarse con tu frontend de Unir.

---

## 🎯 Lo que Necesitas para Enviar/Recibir Mensajes

### 1️⃣ Conectar una Cuenta de Instagram Business (PENDIENTE)

**Acción requerida**: Completar el flujo OAuth

```bash
# Abre esto en tu navegador:
http://localhost:8080/auth/instagram/connect?userId=550e8400-e29b-41d4-a716-446655440000
```

**Qué sucede:**
1. Te redirige a Instagram/Facebook
2. Autorizas tu cuenta Business
3. El sistema guarda tu access token
4. ¡Listo! Ya puedes enviar/recibir

---

## 🚀 API Lista para tu Frontend

### Base URL
```
http://localhost:8080
```

### Endpoints Disponibles para tu Dashboard "Unir"

#### 🔐 Autenticación / Gestión de Cuentas

```javascript
// 1. Conectar cuenta de Instagram (botón en tu UI)
window.location.href = `http://localhost:8080/auth/instagram/connect?userId=${userId}`;

// 2. Verificar estado de conexión (mostrar en UI)
GET /auth/instagram/status?userId={userId}
Response: {
  "connected": true,
  "accountId": "uuid",
  "username": "tutienda_oficial",
  "displayName": "Tu Tienda"
}

// 3. Desconectar cuenta
POST /auth/instagram/disconnect?userId={userId}
```

---

#### 💬 Conversaciones (Para tu Conversation List)

```javascript
// Listar todas las conversaciones
GET /v1/threads?page=0&size=20

Response:
{
  "content": [
    {
      "id": "thread-uuid",
      "channel": "INSTAGRAM",
      "participants": [
        {
          "id": "instagram_user_id",
          "name": "Cliente Juan"
        }
      ],
      "lastMessageAt": "2025-10-14T10:30:00Z"
    }
  ],
  "totalElements": 45
}

// Filtrar por canal
GET /v1/threads?channel=INSTAGRAM

// Buscar conversaciones
GET /v1/threads?q=Juan
```

---

#### 📨 Mensajes (Para tu Chat Window)

```javascript
// Obtener mensajes de una conversación
GET /v1/threads/{threadId}/messages?page=0&size=50

Response:
{
  "content": [
    {
      "id": "message-uuid",
      "direction": "INBOUND", // o "OUTBOUND"
      "bodyText": "Hola! Quiero hacer una consulta",
      "sender": {
        "id": "instagram_user_id",
        "name": "Cliente Juan"
      },
      "createdAt": "2025-10-14T10:30:00Z",
      "status": "received"
    }
  ]
}

// Enviar mensaje
POST /v1/messages
Content-Type: application/json

{
  "channel": "INSTAGRAM",
  "accountId": "account-uuid",
  "to": [
    {
      "id": "instagram_user_id"
    }
  ],
  "text": "Gracias por tu mensaje! ¿En qué puedo ayudarte?"
}
```

---

#### 🔔 Tiempo Real (Server-Sent Events)

```javascript
// Conectar al stream de eventos
const eventSource = new EventSource('http://localhost:8080/v1/stream');

// Nuevo mensaje recibido
eventSource.addEventListener('message.received', (event) => {
  const message = JSON.parse(event.data);
  console.log('Nuevo mensaje:', message);
  
  // Actualizar UI: agregar mensaje al chat
  addMessageToConversation(message.threadId, message);
  
  // Mostrar notificación
  showNotification(`Nuevo mensaje de ${message.sender.name}`);
  
  // Reproducir sonido
  playNotificationSound();
});

// Mensaje enviado confirmado
eventSource.addEventListener('message.sent', (event) => {
  const message = JSON.parse(event.data);
  // Actualizar status del mensaje en UI
  updateMessageStatus(message.id, 'sent');
});

// Heartbeat (mantener conexión viva)
eventSource.addEventListener('heartbeat', (event) => {
  console.log('Conexión activa');
});

// Errores
eventSource.onerror = (error) => {
  console.error('Error en SSE:', error);
  // Reconectar después de 5 segundos
  setTimeout(() => window.location.reload(), 5000);
};
```

---

## 🎨 Integración con tu Frontend "Unir"

### Componente: Sidebar (Selección de Canal)

```javascript
// En tu componente de selección de canal
const [channels, setChannels] = useState([
  { id: 'instagram', name: 'Instagram', connected: false },
  { id: 'whatsapp', name: 'WhatsApp', connected: false },
  { id: 'gmail', name: 'Gmail', connected: false }
]);

useEffect(() => {
  // Verificar estado de Instagram
  const checkInstagramStatus = async () => {
    const response = await fetch(
      `http://localhost:8080/auth/instagram/status?userId=${currentUserId}`
    );
    const data = await response.json();
    
    setChannels(prev => prev.map(ch => 
      ch.id === 'instagram' 
        ? { ...ch, connected: data.connected, username: data.username }
        : ch
    ));
  };
  
  checkInstagramStatus();
}, [currentUserId]);
```

---

### Componente: Conversation List

```javascript
const ConversationList = ({ selectedChannel }) => {
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadConversations = async () => {
      setLoading(true);
      
      const params = new URLSearchParams({
        page: 0,
        size: 20,
        ...(selectedChannel !== 'all' && { channel: selectedChannel })
      });
      
      const response = await fetch(
        `http://localhost:8080/v1/threads?${params}`
      );
      const data = await response.json();
      
      setConversations(data.content);
      setLoading(false);
    };
    
    loadConversations();
  }, [selectedChannel]);

  return (
    <div className="conversation-list">
      {loading ? (
        <div>Cargando...</div>
      ) : (
        conversations.map(conv => (
          <ConversationItem 
            key={conv.id}
            conversation={conv}
            onClick={() => setSelectedConversation(conv.id)}
          />
        ))
      )}
    </div>
  );
};
```

---

### Componente: Chat Window

```javascript
const ChatWindow = ({ threadId, accountId }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);

  // Cargar mensajes
  useEffect(() => {
    if (!threadId) return;
    
    const loadMessages = async () => {
      const response = await fetch(
        `http://localhost:8080/v1/threads/${threadId}/messages?size=50`
      );
      const data = await response.json();
      setMessages(data.content);
    };
    
    loadMessages();
  }, [threadId]);

  // Conectar a SSE para mensajes en tiempo real
  useEffect(() => {
    const eventSource = new EventSource('http://localhost:8080/v1/stream');
    
    eventSource.addEventListener('message.received', (event) => {
      const message = JSON.parse(event.data);
      
      // Solo agregar si es del thread actual
      if (message.threadId === threadId) {
        setMessages(prev => [...prev, message]);
      }
    });
    
    return () => eventSource.close();
  }, [threadId]);

  // Enviar mensaje
  const handleSendMessage = async () => {
    if (!newMessage.trim()) return;
    
    setSending(true);
    
    try {
      const response = await fetch('http://localhost:8080/v1/messages', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          channel: 'INSTAGRAM',
          accountId: accountId,
          threadId: threadId,
          to: [{ id: messages[0].sender.id }],
          text: newMessage
        })
      });
      
      if (response.ok) {
        setNewMessage('');
        // El mensaje se agregará via SSE
      }
    } catch (error) {
      console.error('Error enviando mensaje:', error);
      alert('Error enviando mensaje');
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="chat-window">
      <div className="messages">
        {messages.map(msg => (
          <MessageBubble 
            key={msg.id}
            message={msg}
            isOwn={msg.direction === 'OUTBOUND'}
          />
        ))}
      </div>
      
      <div className="message-input">
        <input
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
          placeholder="Escribe un mensaje..."
          disabled={sending}
        />
        <button 
          onClick={handleSendMessage}
          disabled={sending || !newMessage.trim()}
        >
          {sending ? 'Enviando...' : 'Enviar'}
        </button>
      </div>
    </div>
  );
};
```

---

## 🔧 Configuración Frontend

### 1. Variables de Entorno

Crea un archivo `.env` en tu frontend:

```env
# Desarrollo local
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080

# Producción (cuando deploys)
# NEXT_PUBLIC_API_URL=https://tu-api.railway.app
```

### 2. Cliente API Helper

```javascript
// lib/api.js
const API_URL = process.env.NEXT_PUBLIC_API_URL;

export const api = {
  // OAuth
  connectInstagram: (userId) => {
    window.location.href = `${API_URL}/auth/instagram/connect?userId=${userId}`;
  },
  
  getInstagramStatus: async (userId) => {
    const res = await fetch(`${API_URL}/auth/instagram/status?userId=${userId}`);
    return res.json();
  },
  
  // Threads
  getThreads: async (params = {}) => {
    const query = new URLSearchParams(params);
    const res = await fetch(`${API_URL}/v1/threads?${query}`);
    return res.json();
  },
  
  getThread: async (threadId) => {
    const res = await fetch(`${API_URL}/v1/threads/${threadId}`);
    return res.json();
  },
  
  // Messages
  getMessages: async (threadId, page = 0, size = 50) => {
    const res = await fetch(
      `${API_URL}/v1/threads/${threadId}/messages?page=${page}&size=${size}`
    );
    return res.json();
  },
  
  sendMessage: async (messageData) => {
    const res = await fetch(`${API_URL}/v1/messages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(messageData)
    });
    return res.json();
  },
  
  // SSE
  connectSSE: () => {
    return new EventSource(`${API_URL}/v1/stream`);
  }
};
```

---

## 🧪 Testing de Integración

### Paso 1: Conectar Instagram

```javascript
// En tu componente de settings/accounts
const handleConnectInstagram = () => {
  api.connectInstagram('550e8400-e29b-41d4-a716-446655440000');
};
```

### Paso 2: Verificar Conexión

```javascript
useEffect(() => {
  const checkStatus = async () => {
    const status = await api.getInstagramStatus(currentUserId);
    console.log('Instagram status:', status);
  };
  checkStatus();
}, []);
```

### Paso 3: Cargar Conversaciones

```javascript
useEffect(() => {
  const loadConversations = async () => {
    const data = await api.getThreads({ channel: 'INSTAGRAM' });
    console.log('Conversaciones:', data.content);
  };
  loadConversations();
}, []);
```

### Paso 4: Enviar Mensaje de Prueba

```javascript
const testSendMessage = async () => {
  const result = await api.sendMessage({
    channel: 'INSTAGRAM',
    accountId: 'tu-account-uuid',
    to: [{ id: 'instagram-user-id' }],
    text: 'Hola! Este es un mensaje de prueba desde Unir'
  });
  console.log('Mensaje enviado:', result);
};
```

---

## ✅ Checklist de Integración

- [ ] **Conectar cuenta de Instagram Business** (OAuth)
- [ ] Configurar variables de entorno en frontend
- [ ] Implementar cliente API
- [ ] Integrar sidebar con verificación de canales conectados
- [ ] Implementar lista de conversaciones con filtros
- [ ] Implementar ventana de chat con mensajes
- [ ] Conectar SSE para tiempo real
- [ ] Agregar notificaciones de nuevos mensajes
- [ ] Implementar búsqueda de conversaciones
- [ ] Agregar indicadores de "escribiendo..."
- [ ] Implementar panel de colaboradores (futuro)

---

## 🎯 Próximos Pasos

### 1. AHORA: Conectar tu Cuenta
```
http://localhost:8080/auth/instagram/connect?userId=550e8400-e29b-41d4-a716-446655440000
```

### 2. Probar Endpoints con Postman/cURL
```bash
# Ver threads
curl http://localhost:8080/v1/threads

# Ver status de conexión
curl "http://localhost:8080/auth/instagram/status?userId=550e8400-e29b-41d4-a716-446655440000"
```

### 3. Integrar con tu Frontend "Unir"
- Usar los ejemplos de código de arriba
- Adaptar el diseño (colores rosa magenta + verde lima)
- Agregar el logo de Unir

### 4. Testing E2E
- Recibir un mensaje real de Instagram
- Enviar una respuesta desde tu UI
- Verificar que llegue al cliente en Instagram

---

## 🚨 IMPORTANTE: CORS

Tu API Gateway ya tiene CORS configurado para:
```
http://localhost:3000
http://localhost:3001
```

Si tu frontend corre en otro puerto, actualiza en:
```yaml
# api-gateway/src/main/resources/application.yml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001,http://localhost:5173
```

---

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs: `tail -f api-gateway.log`
2. Verifica health: `curl http://localhost:8080/health`
3. Revisa la documentación completa en `FRONTEND_API_GUIDE.md`

---

**¿Listo para conectar tu cuenta y empezar a probar con tu frontend?**

