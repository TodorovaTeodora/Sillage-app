const localNoteImages: Record<string, string> = {
  akigalawood: 'akigalawood.jpg',
  almond: 'almond.jpg',
  ambergris: 'ambergris.jpg',
  ambrette: 'ambrette.jpg',
  benzoin: 'benzoin.jpg',
  'blood orange': 'blood-orange.jpg',
  cardamom: 'cardamom.jpg',
  'coconut milk': 'coconut-milk.webp',
  cognac: 'cognac.webp',
  cypriol: 'cypriol.jpg',
  davana: 'davana.webp',
  frankincense: 'frankincense.jpg',
  georgywood: 'georgywood.jpg',
  hazelnut: 'hazelnut.jpg',
  heliotrope: 'heliotrope.jpg',
  jasmine: 'jasmine.jpg',
  'jasmine sambac': 'jasmine-sambac.jpg',
  leather: 'leather.jpg',
  peach: 'peach.jpg',
  pear: 'pear.jpg',
  'pine tree': 'pine-tree.jpg',
  pineapple: 'pineapple.jpg',
  raspberry: 'raspberry.jpg',
  resin: 'resin.jpg',
  'rose water': 'rose-water.jpg',
  rum: 'rum.webp',
  saffron: 'saffron.jpg',
  sandalwood: 'sandalwood.jpg',
  'tonka bean': 'tonka-bean.jpg',
  vetiver: 'vetiver.jpg',
}

const aliases: Record<string, string> = {
  'ambrette seed': 'ambrette',
  'ambrette seeds': 'ambrette',
  'bitter almond': 'almond',
  coconut: 'coconut milk',
  'musk mallow': 'ambrette',
  nagarmotha: 'cypriol',
  olibanum: 'frankincense',
  pine: 'pine tree',
  'pine needle': 'pine tree',
  'pine needles': 'pine tree',
  rosewater: 'rose water',
  raspberries: 'raspberry',
  resins: 'resin',
  'sambac jasmine': 'jasmine sambac',
  tonka: 'tonka bean',
}

function normalize(note: string) {
  return note
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ' ')
    .trim()
}

export function getLocalNoteImageUrl(note: string) {
  const normalized = normalize(note)
  const filename = localNoteImages[aliases[normalized] ?? normalized]
  return filename ? `/note-images/${filename}` : null
}
