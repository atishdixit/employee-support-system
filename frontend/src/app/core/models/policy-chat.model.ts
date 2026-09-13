export type PolicyCategory =
  | 'LEAVE'
  | 'HOLIDAY'
  | 'MATERNITY_PATERNITY'
  | 'PROMOTION'
  | 'WORK_HOURS'
  | 'GENERAL'
  | 'OUT_OF_SCOPE';

export interface PolicyChatRequest {
  question: string;
}

export interface TokenUsage {
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
}

export interface PolicyChatResponse {
  answer: string;
  category: PolicyCategory;
  employeeContext: string;
  correlationId: string;
  tokenUsage: TokenUsage | null;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
  category?: PolicyCategory;
  correlationId?: string;
}
