// Pixel GUI kit for Magical Minions concept screens, in the Hearthwrought design system.
// Drawn in GUI pixels (Minecraft GUI scale) times S. Sprites come from the design system (sprites.js -> DS).
const S = 3;
const FONT = "'Pixelify Sans'"; // stand-in for Minecraft's own font, which the real GUI keeps
let ctx, CW, CH;

const T = { // design-system tokens
  soot0: '#120e0c', soot1: '#1b1613', soot2: '#2b2320', soot3: '#40352f', soot4: '#5a4c43',
  ink: '#f8f0d8', muted: '#cdb98f',
  brass0: '#5c3d12', brass1: '#94681f', brass2: '#c9993a', brass3: '#eac466', brass4: '#fbe9a8',
  wood0: '#4f3d2a', wood1: '#7d6444', wood2: '#a98c61', wood3: '#cfb68a',
  fox0: '#0f4a40', fox1: '#1f8a72', fox2: '#3fd0a8', fox3: '#8af5d4', fox4: '#e0fff4',
  kiln2: '#ec7424', kiln3: '#ffb347',
  madder2: '#96222e', madder3: '#c93a40', madder4: '#e8706a',
  leather1: '#5e3b22', leather2: '#875a33', leather3: '#b07f4e',
  stone1: '#3a373b', stone2: '#565255', stone3: '#77726f',
  indigo1: '#222e5e', indigo3: '#5068b8', moss3: '#8dbf4e', glass3: '#b0def0', bone3: '#e3d9c0',
};

function setup(w, h) {
  const cv = document.getElementById('c');
  cv.width = w * S; cv.height = h * S; CW = w; CH = h;
  ctx = cv.getContext('2d'); ctx.imageSmoothingEnabled = false;
}
function rect(x, y, w, h, c) { ctx.fillStyle = c; ctx.fillRect(Math.round(x * S), Math.round(y * S), Math.round(w * S), Math.round(h * S)); }
function px(x, y, c) { rect(x, y, 1, 1, c); }
function frame(x, y, w, h, c) { rect(x, y, w, 1, c); rect(x, y + h - 1, w, 1, c); rect(x, y, 1, h, c); rect(x + w - 1, y, 1, h, c); }
function dashed(x, y, w, h, c) { for (let i = 0; i < w; i += 2) { px(x + i, y, c); px(x + i, y + h - 1, c); } for (let i = 0; i < h; i += 2) { px(x, y + i, c); px(x + w - 1, y + i, c); } }
function bevel(x, y, w, h, fill, hi, lo, outline) {
  if (outline) { rect(x - 1, y, w + 2, h, outline); rect(x, y - 1, w, h + 2, outline); }
  rect(x, y, w, h, fill);
  rect(x, y, w - 1, 1, hi); rect(x, y, 1, h - 1, hi);
  rect(x + 1, y + h - 1, w - 1, 1, lo); rect(x + w - 1, y + 1, 1, h - 1, lo);
}
function mulberry(a) { return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }

// GUI frame: soot panel, light from the top-left, brass corner caps with a rivet.
function panel(x, y, w, h) {
  bevel(x, y, w, h, T.soot2, T.soot4, T.soot1, T.soot0);
  rect(x + 1, y + 1, w - 3, 1, T.soot3); rect(x + 1, y + 1, 1, h - 3, T.soot3);
  for (const [cx, cy, fx, fy] of [[x, y, 1, 1], [x + w - 5, y, -1, 1], [x, y + h - 5, 1, -1], [x + w - 5, y + h - 5, -1, -1]]) {
    rect(cx, cy, 5, 5, T.brass2);
    rect(fx > 0 ? cx : cx + 4, cy, 1, 5, fx > 0 ? T.brass3 : T.brass1);
    rect(cx, fy > 0 ? cy : cy + 4, 5, 1, fy > 0 ? T.brass3 : T.brass1);
    px(cx + 2, cy + 2, T.brass0);
  }
}
function inset(x, y, w, h, fill) { bevel(x, y, w, h, fill || T.soot1, T.soot0, T.soot3); }
function divider(x, y, w) { rect(x, y, w, 1, T.soot1); rect(x, y + 1, w, 1, T.soot3); }

function slot(x, y, o = {}) {
  const n = o.size || 18;
  inset(x, y, n, n);
  if (o.item) sprite(o.item, x + n / 2, y + n / 2, { center: true, fit: n - 2, alpha: o.ghost ? 0.35 : null });
  if (o.count) text(String(o.count), x + n - 1, y + n - 8, { align: 'right', size: 8 });
  if (o.hover) rect(x + 1, y + 1, n - 2, n - 2, 'rgba(248,240,216,0.18)');
  if (o.selected) { frame(x - 1, y - 1, n + 2, n + 2, T.brass3); frame(x, y, n, n, T.brass1); }
  if (o.locked) {
    rect(x + 1, y + 1, n - 2, n - 2, 'rgba(18,14,12,0.6)');
    dashed(x + 1, y + 1, n - 2, n - 2, T.soot4);
  }
}

function text(s, x, y, o = {}) {
  const size = (o.size || 8) * S;
  ctx.font = `${o.bold ? 600 : 500} ${size}px ${FONT}`;
  ctx.textBaseline = 'top'; ctx.textAlign = o.align || 'left';
  const X = x * S, Y = (y - 1) * S;
  if (o.shadow !== false) { ctx.fillStyle = T.soot0; ctx.fillText(s, X + S, Y + S); }
  ctx.fillStyle = o.color || T.ink; ctx.fillText(s, X, Y);
  return ctx.measureText(s).width / S;
}
function measure(s, size = 8) { ctx.font = `500 ${size * S}px ${FONT}`; return ctx.measureText(s).width / S; }

function button(x, y, w, h, label, o = {}) {
  if (o.primary) { bevel(x, y, w, h, T.brass2, T.brass3, T.brass1, T.soot0); px(x + 1, y + 1, T.brass4); }
  else if (o.dim) bevel(x, y, w, h, T.soot2, T.soot3, T.soot1, T.soot0);
  else bevel(x, y, w, h, T.leather2, T.leather3, T.leather1, T.soot0);
  text(label, x + w / 2, y + Math.round((h - 8) / 2) + 1, { align: 'center', size: o.size || 8, color: o.primary ? T.soot1 : o.dim ? T.soot4 : T.ink, shadow: !o.primary && !o.dim });
}
function chip(x, y, label, fill, ink) {
  const w = measure(label, 6) + 6;
  bevel(x, y, w, 10, fill, 'rgba(248,240,216,0.25)', 'rgba(18,14,12,0.5)');
  text(label, x + 3, y + 1, { size: 6, color: ink, shadow: false });
  return w;
}
function tooltip(x, y, lines) {
  const w = Math.max(...lines.map(l => measure(l.t, l.s || 8))) + 8, h = lines.length * 10 + 4;
  rect(x + 1, y, w - 2, h, 'rgba(18,14,12,0.96)'); rect(x, y + 1, w, h - 2, 'rgba(18,14,12,0.96)');
  frame(x + 1, y + 1, w - 2, h - 2, T.brass1);
  lines.forEach((l, i) => text(l.t, x + 4, y + 3 + i * 10 + (i ? 1 : 0), { color: l.c || T.ink, size: l.s || 8 }));
}

// ---------------------------------------------------------------------------
// Design-system sprites: DS[name] = {b: base png, g: glow png, w, h}
const IM = {};
async function loadSprites() {
  await Promise.all(Object.entries(DS).map(([k, v]) => Promise.all(['b', 'g'].map(l => v[l] && new Promise(r => {
    const i = new Image(); i.onload = () => { IM[k + '|' + l] = i; r(); }; i.onerror = r; i.src = v[l];
  })))));
}
// Draw sprite `name`. o: {scale, center, fit, glow (default true), alpha, crop:[sx,sy,sw,sh], flip}
function sprite(name, x, y, o = {}) {
  const d = DS[name]; if (!d) { console.warn('missing sprite', name); return; }
  const [sx, sy, sw, sh] = o.crop || [0, 0, d.w, d.h];
  let k = o.scale || 1;
  if (o.fit) k = Math.max(sw, sh) > o.fit ? o.fit / Math.max(sw, sh) : Math.max(1, Math.floor(o.fit / Math.max(sw, sh)));
  const w = sw * k, h = sh * k;
  if (o.center) { x -= w / 2; y -= h / 2; }
  x = Math.round(x); y = Math.round(y);
  ctx.save();
  if (o.alpha != null) ctx.globalAlpha = o.alpha;
  if (o.flip) { ctx.translate((x * 2 + w) * S, 0); ctx.scale(-1, 1); }
  const b = IM[name + '|b'], g = IM[name + '|g'];
  if (g && o.glow !== false) { // bloom only the glow layer, as the design system's sheets do
    ctx.save(); ctx.globalCompositeOperation = 'screen'; ctx.filter = `blur(${1.6 * Math.max(1, k) * S}px)`;
    ctx.drawImage(g, sx, sy, sw, sh, x * S, y * S, w * S, h * S); ctx.restore();
  }
  ctx.imageSmoothingEnabled = false;
  ctx.drawImage(b, sx, sy, sw, sh, x * S, y * S, w * S, h * S);
  ctx.restore();
}
// Head crops of lineage sprites, for style tabs and roster rows.
const FACE = {
  'Rune Golem (Medium)': [6, 0, 22, 20], 'Ember Imp (Small)': [4, 0, 22, 20], 'Bonebound Acolyte (Medium)': [8, 0, 22, 20],
  'Starlight Wisp (Small)': [2, 4, 22, 20], 'Mossling (Medium)': [6, 0, 22, 20],
};
function face(lineage, x, y, size = 16) { sprite(lineage, x, y, { crop: FACE[lineage], fit: size }); }

// ---------------------------------------------------------------------------
// Dimmed in-game night world behind a screen (vanilla-ish blocks).
function world(dim = 0.62, seed = 7) {
  const rnd = mulberry(seed), W = CW, H = CH;
  for (let y = 0; y < H; y += 2) { const t = y / H; rect(0, y, W, 2, `rgb(${18 + t * 30 | 0},${22 + t * 34 | 0},${44 + t * 40 | 0})`); }
  for (let i = 0; i < 120; i++) px(rnd() * W | 0, rnd() * H * 0.5 | 0, rnd() < 0.3 ? '#fffdf2' : '#b9c4d8');
  rect(W * 0.8, 16, 12, 12, '#e8e6d8'); rect(W * 0.8 + 3, 19, 3, 3, '#c9c6b4');
  const B = 8; let hgt = H * 0.62;
  for (let x = 0; x < W; x += B) {
    hgt += (rnd() - 0.5) * 10; hgt = Math.max(H * 0.52, Math.min(H * 0.76, hgt));
    const top = Math.round(hgt / B) * B;
    for (let y = top; y < H; y += B) {
      const grass = y === top;
      rect(x, y, B, B, grass ? '#4f8a3a' : (y > top + 24 ? '#6a6a6a' : '#79553a'));
      rect(x, y, B, 1, grass ? '#6aa84a' : 'rgba(255,255,255,0.07)'); rect(x, y + B - 1, B, 1, 'rgba(0,0,0,0.2)');
    }
    if (rnd() < 0.07) { rect(x + 2, top - 24, 4, 24, '#6b4a2a'); rect(x - 8, top - 40, 24, 18, '#2f5e2a'); rect(x - 4, top - 48, 16, 10, '#356a30'); }
  }
  rect(0, 0, W, H, `rgba(10,8,6,${dim})`);
}

// Simple vanilla-style sprites for the player's own items (the mod's UI never uses them as icons).
const VAN = {
  sword: ['.............kk.', '............kSsk', '...........kSsk.', '..........kSsk..', '.........kSsk...', '........kSsk....', '.......kSsk.....', '..kk..kSsk......', '..kyk.kSk.......', '...kykSk........', '....kyk.........', '...kwkyk........', '..kwk.kyk.......', '.kkk...kk.......'],
  pick: ['...kkkkkkkk.....', '..kpPPPPPPpk....', '.kpk.kkkkkpk....', '.kk...kwk.kk....', '......kwk.......', '.....kwk........', '.....kwk........', '....kwk.........', '....kwk.........', '...kwk..........', '...kwk..........', '..kwk...........', '..kk............'],
  bread: ['.....kkkkkk.....', '...kkWWWWWWkk...', '..kWWwWWwWWwWk..', '.kWwWWwWWwWWwWk.', '.kWWWWWWWWWWWnk.', '.kwwwwwwwwwwnnk.', '..knnnnnnnnnnk..', '...kkkkkkkkkk...'],
  torch: ['.......kk.......', '......kFFk......', '......kFfk......', '......kffk......', '......kxxk......', '.......kk.......', '.......wk.......', '.......wk.......', '.......wk.......', '.......wk.......', '.......wk.......', '.......kk.......'],
  blaze: ['................', '.......kk.......', '......kfFk......', '.....kfFFfk.....', '....kxfFFfxk....', '...kxffFFffxk...', '...kxfffffffk...', '...kxxfffffxk...', '....kxxffxxk....', '.....kkkkkk.....'],
  cobble: ['kkkkkkkkkkkkkkkk', 'kEEeeEdEEEeeEdek', 'kEeeedEEeeeedEek', 'kdddEEeeeddEEeek', 'kEEeeeedEEeeeddk', 'keeddEEEeeeEEeek', 'kEEEeeeddEEeeedk', 'kdeeeEEEeeeddEEk', 'kEEeddeeeEEEeeek', 'keeEEEeeddeeeEEk', 'kdEEeeeEEEeddeek', 'kEeeddEEeeeEEEek', 'keEEEeeeddEEeedk', 'kddeeEEEeeeeEEek', 'kEEeeeeddEEeeeek', 'kkkkkkkkkkkkkkkk'],
};
const VPAL = { k: '#1b1613', S: '#f4f4f4', s: '#c8d2e6', y: '#c9993a', w: '#8a5a32', W: '#b07a46', n: '#4f3219', p: '#5ab4ff', P: '#c4ecff', F: '#fff0a8', f: '#ffb347', x: '#d8522c', E: '#8e9ead', e: '#5a6b7a', d: '#39434d' };
function van(name, x, y) {
  const rows = VAN[name], oy = Math.floor((16 - rows.length) / 2);
  rows.forEach((r, j) => { for (let i = 0; i < r.length; i++) if (r[i] !== '.') px(x + i, y + oy + j, VPAL[r[i]]); });
}
function invSlot(x, y, it) {
  inset(x, y, 18, 18);
  if (!it) return;
  const [name, count] = it;
  if (VAN[name]) van(name, x + 1, y + 1); else sprite(name, x + 9, y + 9, { center: true, fit: 16 });
  if (count > 1) text(String(count), x + 17, y + 10, { align: 'right' });
}
function inventory(x, y, inv, hot, sel) {
  for (let r = 0; r < 3; r++) for (let c = 0; c < 9; c++) invSlot(x + c * 18, y + r * 18, inv[r * 9 + c]);
  hot.forEach((it, c) => invSlot(x + c * 18, y + 58, it));
  if (sel != null) frame(x + sel * 18 - 1, y + 57, 20, 20, T.brass3);
}

const HEART = ['.kk.kk.', 'kaakaak', 'kaaaaak', 'kaaaaak', '.kaaak.', '..kak..', '...k...'];
function heart(x, y, on) { HEART.forEach((r, j) => { for (let i = 0; i < 7; i++) if (r[i] !== '.') px(x + i, y + j, r[i] === 'k' ? T.soot0 : (on ? T.fox2 : T.soot3)); }); }
function hearts(x, y, n, max = 5) { for (let i = 0; i < max; i++) heart(x + i * 8, y, i < n); }

async function ready() { await document.fonts.load(`500 24px ${FONT}`); await loadSprites(); }
