// Coze Voice Gen API 客户端
export interface VoiceGenerationRequest {
  text: string;
  voice_id: string;
  language?: string;
  speed?: number;
}

export interface VoiceGenerationResponse {
  audio_url: string;
  duration: number;
}

export class CozeVoiceClient {
  private apiKey: string;
  private baseUrl: string;

  constructor(apiKey: string, baseUrl: string = 'https://api.coze.com') {
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
  }

  async generateVoice(request: VoiceGenerationRequest): Promise<VoiceGenerationResponse> {
    const response = await fetch(`${this.baseUrl}/voice/generate`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(request)
    });

    if (!response.ok) {
      throw new Error(`Coze API error: ${response.status} ${response.statusText}`);
    }

    const data = await response.json();
    return {
      audio_url: data.audio_url || data.data?.audio_url,
      duration: data.duration || this.estimateDuration(request.text)
    };
  }

  private estimateDuration(text: string): number {
    // 简单估算：中文每分钟200字，英文每分钟150词
    const chineseChars = (text.match(/[\u4e00-\u9fff]/g) || []).length;
    const englishWords = text.replace(/[^a-zA-Z\s]/g, '').split(/\s+/).filter(w => w.length > 0).length;
    return Math.max(1, Math.ceil((chineseChars / 200 + englishWords / 150) * 60));
  }
}