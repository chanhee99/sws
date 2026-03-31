export interface QuestionDto {
  id: number
  text: string
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'SHORT_TEXT' | 'LONG_TEXT'
  options?: string[]
  required: boolean
  sortOrder: number
}

export interface SurveyDto {
  id: number
  title: string
  description?: string
  active: boolean
  createdAt: string
  questions: QuestionDto[]
}

export interface UpsertSurveyRequest {
  title: string
  description?: string
  active: boolean
  questions: Array<{
    id?: number
    text: string
    type: QuestionDto['type']
    options?: string[]
    required: boolean
    sortOrder: number
  }>
}

export interface AnswerInput {
  questionId: number
  value: string
}

export interface SubmitSurveyRequest {
  surveyId: number
  respondentId?: string
  answers: AnswerInput[]
}

export interface ResponseListItemDto {
  id: number
  surveyId: number
  surveyTitle: string
  submittedAt: string
  respondentId?: string
}

export interface AnswerDetailDto {
  questionId: number
  questionText: string
  questionType: string
  value: string
}

export interface ResponseDetailDto {
  id: number
  surveyId: number
  surveyTitle: string
  submittedAt: string
  respondentId?: string
  answers: AnswerDetailDto[]
}

export interface QuestionStatDto {
  questionId: number
  questionText: string
  type: string
  optionCounts?: Record<string, number>
  textAnswers?: string[]
}

export interface StatisticsDto {
  surveyId: number
  surveyTitle: string
  totalResponses: number
  questionStats: QuestionStatDto[]
}

export interface PageDto<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ResultProductDto {
  name: string
  desc: string
}

export interface ResultDto {
  skinType: string
  statusText: string
  productSetTitle: string
  products: ResultProductDto[]
  couponButtonText: string
  couponUrl?: string | null
  couponCode?: string | null
}

export interface SubmitSurveyResponseDto {
  success: boolean
  responseId: number
  message: string
  result: ResultDto
}

export interface ResultProfileDto {
  key: string
  skinTypeLabel: string
  statusText: string
  productSetTitle: string
  products: ResultProductDto[]
  couponButtonText: string
  couponUrl?: string | null
  couponCode?: string | null
  priority: number
  defaultProfile: boolean
}

export interface ResultConditionDto {
  questionId: number
  matchValue: string
  profileKey: string
}

export interface ResultConfigDto {
  profiles: ResultProfileDto[]
  conditions: ResultConditionDto[]
}

export interface ResultConfigUpsertRequest {
  profiles: ResultProfileDto[]
  conditions: ResultConditionDto[]
}
