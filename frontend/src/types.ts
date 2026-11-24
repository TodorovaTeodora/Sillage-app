export type NotePyramid = {
  top: string[]
  heart: string[]
  base: string[]
}

export type Fragrance = {
  id: string
  name: string
  manufacturer: string
  year: number | null
  family: string
  concentration: string
  description: string
  pyramid: NotePyramid
}

export type FragranceFacets = {
  manufacturerCount: number
  noteCount: number
  earliestYear: number
  latestYear: number
  totalCount: number
  manufacturers: string[]
}

export type FragranceFilters = {
  query: string
  manufacturers: string[]
}

export type AuthUser = {
  email: string
  username: string | null
}

export type AccountProfile = {
  email: string
  username: string | null
  country: string | null
  city: string | null
}

export type FragranceComment = {
  id: number
  fragranceId: string
  author: string
  content: string
  createdAt: string
  updatedAt: string
  ownedByCurrentUser: boolean
}

export type MediaAsset = {
  imageUrl: string
  sourcePageUrl: string
  author: string
  licenseName: string
  licenseUrl: string
  alt: string
}

export type FragranceMedia = {
  fragranceImage: MediaAsset | null
  noteImages: Record<string, MediaAsset>
  warnings: string[]
}

export type FragranceImageBatch = {
  images: Record<string, MediaAsset>
  warnings: string[]
}
