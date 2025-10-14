-- Create ENUM types
CREATE TYPE channel_type AS ENUM ('INSTAGRAM', 'WHATSAPP', 'GMAIL');
CREATE TYPE direction_type AS ENUM ('INBOUND', 'OUTBOUND');

-- Create accounts table
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    channel channel_type NOT NULL,
    display_name VARCHAR(255),
    external_ids JSONB,
    credentials_encrypted TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT accounts_channel_check CHECK (channel IN ('INSTAGRAM', 'WHATSAPP', 'GMAIL')),
    CONSTRAINT accounts_status_check CHECK (status IN ('active', 'inactive', 'suspended'))
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_channel ON accounts(channel);
CREATE INDEX idx_accounts_status ON accounts(status);

-- Create threads table
CREATE TABLE threads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    channel channel_type NOT NULL,
    external_thread_id VARCHAR(500) NOT NULL,
    participants JSONB,
    last_message_at TIMESTAMPTZ,
    
    CONSTRAINT threads_channel_check CHECK (channel IN ('INSTAGRAM', 'WHATSAPP', 'GMAIL')),
    CONSTRAINT fk_threads_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT threads_unique_external_thread UNIQUE (account_id, external_thread_id)
);

CREATE INDEX idx_threads_account_id ON threads(account_id);
CREATE INDEX idx_threads_channel ON threads(channel);
CREATE INDEX idx_threads_last_message_at ON threads(last_message_at DESC);
CREATE INDEX idx_threads_external_thread_id ON threads(external_thread_id);

-- Create messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id UUID NOT NULL,
    channel channel_type NOT NULL,
    direction direction_type NOT NULL,
    external_message_id VARCHAR(500),
    sender JSONB,
    body_text TEXT,
    status VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT messages_channel_check CHECK (channel IN ('INSTAGRAM', 'WHATSAPP', 'GMAIL')),
    CONSTRAINT messages_direction_check CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    CONSTRAINT fk_messages_thread FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    CONSTRAINT messages_unique_external_message UNIQUE (thread_id, external_message_id)
);

CREATE INDEX idx_messages_thread_id ON messages(thread_id);
CREATE INDEX idx_messages_channel ON messages(channel);
CREATE INDEX idx_messages_direction ON messages(direction);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
CREATE INDEX idx_messages_status ON messages(status);

