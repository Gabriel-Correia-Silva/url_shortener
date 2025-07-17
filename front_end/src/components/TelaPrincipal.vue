<template>
  <div class="container">
    <header class="header">
      <h1>🔗 Encurtador de URL</h1>
      <p>Transforme URLs longas em links curtos e personalizados</p>
    </header>

    <main class="main-card">
      <!-- Formulário de Encurtamento -->
      <form @submit.prevent="shortenUrl" class="url-form">
        <input
          v-model="originalUrl"
          type="url"
          placeholder="Digite ou cole sua URL aqui..."
          class="url-input"
          required
          :disabled="loading"
        />
        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="loading"></span>
          <span v-else>Encurtar URL</span>
        </button>
      </form>

      <!-- Mensagens de Erro/Sucesso -->
      <transition name="fade">
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </transition>
      <transition name="fade">
        <div v-if="successMessage" class="success-message">
          {{ successMessage }}
        </div>
      </transition>

      <!-- Resultado do Encurtamento -->
      <transition name="fade">
        <div v-if="shortenedUrl" class="result-card">
          <div class="result-title">✅ URL Encurtada com Sucesso!</div>
          <div class="result-url">
            <input
              ref="shortenedUrlInput"
              :value="shortenedUrl"
              type="text"
              readonly
            />
            <button @click="copyToClipboard" class="copy-btn">
              {{ copyButtonText }}
            </button>
          </div>
        </div>
      </transition>

      <!-- Seção de Estatísticas -->
      <section class="stats-section">
        <h2 class="stats-title">📊 Estatísticas da URL</h2>
        
        <div class="stats-form">
          <input
            v-model="statsUrlId"
            type="text"
            placeholder="Digite o ID da URL encurtada..."
            class="stats-input"
            :disabled="statsLoading"
          />
          <button @click="getStats" class="stats-btn" :disabled="statsLoading">
            <span v-if="statsLoading" class="loading"></span>
            <span v-else>Buscar Stats</span>
          </button>
        </div>

        <transition name="fade">
          <div v-if="urlStats" class="stats-card">
            <div class="stats-item">
              <span class="stats-label">ID:</span>
              <span class="stats-value">{{ urlStats.id }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">URL Original:</span>
              <span class="stats-value">{{ urlStats.fullUrl }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">Expira em:</span>
              <span class="stats-value">{{ formatDate(urlStats.expiresAt) }}</span>
            </div>
            <div class="stats-item">
              <span class="stats-label">Status:</span>
              <span class="stats-value">{{ getUrlStatus(urlStats.expiresAt) }}</span>
            </div>
          </div>
        </transition>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// --- ESTADO REATIVO ---
const originalUrl = ref('');
const shortenedUrl = ref('');
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const copyButtonText = ref('Copiar');
const statsUrlId = ref('');
const urlStats = ref(null);
const statsLoading = ref(false);
const apiBaseUrl = 'http://localhost:8080'; // URL do seu backend
const shortenedUrlInput = ref(null); // ref para o elemento input

// --- FUNÇÕES (MÉTODOS) ---

// Limpa as mensagens de feedback
const clearMessages = () => {
    errorMessage.value = '';
    successMessage.value = '';
};

// Mostra uma mensagem e a esconde após um tempo
const showMessage = (message, type = 'error', duration = 5000) => {
    clearMessages();
    if (type === 'error') {
        errorMessage.value = message;
    } else {
        successMessage.value = message;
    }
    setTimeout(clearMessages, duration);
};

// Encurta a URL
const shortenUrl = async () => {
    if (!originalUrl.value.trim()) {
        showMessage('Por favor, insira uma URL válida.');
        return;
    }
    loading.value = true;
    clearMessages();
    urlStats.value = null; // Limpa stats antigas

    try {
        const response = await axios.post(`${apiBaseUrl}/api/shorten-url`, {
            url: originalUrl.value
        });
        shortenedUrl.value = response.data.url;
        showMessage('URL encurtada com sucesso!', 'success');
        
        const urlParts = shortenedUrl.value.split('/');
        statsUrlId.value = urlParts[urlParts.length - 1];

    } catch (error) {
        console.error('Erro ao encurtar URL:', error);
        if (error.response) {
            switch (error.response.status) {
                case 400: showMessage('URL inválida. Verifique se está no formato correto.'); break;
                case 500: showMessage('Erro interno do servidor. Tente novamente.'); break;
                default: showMessage('Erro ao encurtar URL. Tente novamente.');
            }
        } else if (error.request) {
            showMessage('Não foi possível conectar ao servidor. Verifique sua conexão.');
        } else {
            showMessage('Erro inesperado. Tente novamente.');
        }
    } finally {
        loading.value = false;
    }
};

// Busca as estatísticas da URL
const getStats = async () => {
    if (!statsUrlId.value.trim()) {
        showMessage('Por favor, insira o ID da URL.');
        return;
    }
    statsLoading.value = true;
    clearMessages();
    urlStats.value = null;

    try {
        const response = await axios.get(`${apiBaseUrl}/api/stats/${statsUrlId.value}`);
        urlStats.value = response.data;
    } catch (error) {
        console.error('Erro ao buscar estatísticas:', error);
        if (error.response && error.response.status === 404) {
            showMessage('URL não encontrada ou expirada.');
        } else {
            showMessage('Erro ao buscar estatísticas. Tente novamente.');
        }
    } finally {
        statsLoading.value = false;
    }
};

// Copia a URL para a área de transferência
const copyToClipboard = async () => {
    try {
        await navigator.clipboard.writeText(shortenedUrl.value);
        copyButtonText.value = 'Copiado!';
    } catch (error) {
        console.error('Erro ao copiar com a API moderna:', error);
        try {
           shortenedUrlInput.value.select();
           document.execCommand('copy');
           copyButtonText.value = 'Copiado!';
        } catch (fallbackError) {
            console.error('Erro no fallback de cópia:', fallbackError);
            showMessage('Não foi possível copiar a URL.');
        }
    } finally {
        setTimeout(() => {
           copyButtonText.value = 'Copiar';
        }, 2000);
    }
};

// Formata a data para o padrão pt-BR
const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('pt-BR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
    });
};

// Verifica se a URL está ativa ou expirada
const getUrlStatus = (expiresAt) => {
    if (!expiresAt) return 'N/A';
    return new Date(expiresAt) > new Date() ? '🟢 Ativa' : '🔴 Expirada';
};

// --- CICLO DE VIDA ---
onMounted(() => {
    axios.get(`${apiBaseUrl}/api/stats/test`)
        .catch(() => {
            console.warn('Backend não está respondendo. Verifique se o servidor está rodando em http://localhost:8080');
            showMessage('Não foi possível conectar ao servidor. As funcionalidades podem não operar corretamente.');
        });
});
</script>

<style scoped>
/* Estilos que seriam globais (ex: body) são aplicados aqui de forma escopada.
   Em um projeto real, estilos de body e resets estariam em um arquivo CSS global. */
:global(body) {
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
    color: #333;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding-top: 2rem;
}
:global(*) {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

/* Container principal */
.container {
    max-width: 800px;
    width: 100%;
    margin: 0 auto;
    padding: 2rem;
}

/* Cabeçalho */
.header {
    text-align: center;
    margin-bottom: 3rem;
}
.header h1 {
    color: white;
    font-size: 3rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}
.header p {
    color: rgba(255,255,255,0.9);
    font-size: 1.2rem;
}

/* Card principal com efeito de vidro */
.main-card {
    background: rgba(255,255,255,0.95);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 2.5rem;
    box-shadow: 0 20px 40px rgba(0,0,0,0.1);
    border: 1px solid rgba(255,255,255,0.2);
}

/* Formulário de URL */
.url-form {
    display: flex;
    gap: 1rem;
    margin-bottom: 2rem;
}
.url-input {
    flex: 1;
    padding: 1rem 1.5rem;
    border: 2px solid #e1e5e9;
    border-radius: 12px;
    font-size: 1rem;
    transition: all 0.3s ease;
    background: white;
}
.url-input:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
}

/* Botão de envio */
.submit-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 1rem 2rem;
    border-radius: 12px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    min-width: 140px;
    display: flex;
    justify-content: center;
    align-items: center;
}
.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(102,126,234,0.3);
}
.submit-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

/* Card de resultado */
.result-card {
    background: #f8f9fa;
    border: 1px solid #e9ecef;
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 2rem;
    border-left: 4px solid #28a745;
}
.result-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #28a745;
    margin-bottom: 0.5rem;
}
.result-url {
    display: flex;
    align-items: center;
    gap: 1rem;
    background: white;
    padding: 1rem;
    border-radius: 8px;
    border: 1px solid #dee2e6;
}
.result-url input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 1rem;
    color: #667eea;
    font-weight: 500;
}

.copy-btn {
    background: #667eea;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: all 0.3s ease;
}
.copy-btn:hover {
    background: #5a6fd8;
}

/* Seção de estatísticas */
.stats-section {
    margin-top: 2rem;
}
.stats-title {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 1rem;
    color: #333;
}
.stats-form {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
}
.stats-input {
    flex: 1;
    padding: 0.8rem 1rem;
    border: 2px solid #e1e5e9;
    border-radius: 8px;
    font-size: 0.9rem;
}
.stats-btn {
    background: #17a2b8;
    color: white;
    border: none;
    padding: 0.8rem 1.5rem;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 500;
    display: flex;
    justify-content: center;
    align-items: center;
}
.stats-card {
    background: linear-gradient(135deg, #17a2b8 0%, #138496 100%);
    color: white;
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 1rem;
}
.stats-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
    flex-wrap: wrap;
}
.stats-item:last-child {
    margin-bottom: 0;
}
.stats-label {
    font-weight: 500;
    opacity: 0.9;
    margin-right: 1rem;
}
.stats-value {
    font-weight: 600;
    font-size: 1.1rem;
    word-break: break-all;
}

/* Mensagens de feedback */
.error-message, .success-message {
    padding: 1rem;
    border-radius: 8px;
    margin-bottom: 1rem;
}
.error-message {
    background: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
}
.success-message {
    background: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
}

/* Animação de loading */
.loading {
    display: inline-block;
    width: 20px;
    height: 20px;
    border: 3px solid rgba(255, 255, 255, 0.3);
    border-top: 3px solid #fff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}
@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* Animações de transição */
.fade-enter-active, .fade-leave-active {
    transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
    opacity: 0;
}

/* Responsividade */
@media (max-width: 768px) {
    :global(body) {
        padding-top: 1rem;
    }
    .container {
        padding: 1rem;
    }
    .header h1 {
        font-size: 2.2rem;
    }
    .main-card {
        padding: 1.5rem;
    }
    .url-form, .stats-form, .result-url {
        flex-direction: column;
    }
    .result-url {
        gap: 0.5rem;
    }
}
</style>