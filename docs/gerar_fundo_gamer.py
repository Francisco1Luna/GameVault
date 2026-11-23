# Gera src/gui/img/fundo-gamer.png (fundo da tela de login). Uso: python gerar_fundo_gamer.py fundo-gamer.png  (precisa de Pillow e numpy)
import numpy as np, random, sys
from PIL import Image, ImageDraw, ImageFilter
W,H=1920,1080; HOR=int(H*0.62)
img=np.zeros((H,W,3),float)
# céu: degradê roxo escuro -> magenta perto do horizonte
top=np.array([10,2,30]); mid=np.array([40,10,80]); hor=np.array([130,30,120])
for y in range(HOR):
    t=y/HOR
    c= top+(mid-top)*(t/0.6) if t<0.6 else mid+(hor-mid)*((t-0.6)/0.4)
    img[y,:]=c
# chão escuro
for y in range(HOR,H):
    t=(y-HOR)/(H-HOR); img[y,:]=np.array([20,4,40])*(1-t)+np.array([6,1,16])*t
base=Image.fromarray(img.astype('uint8'))
# estrelas
d=ImageDraw.Draw(base); random.seed(7)
for _ in range(260):
    x=random.randrange(W); y=random.randrange(int(HOR*0.85)); r=random.choice([1,1,1,2])
    a=random.randint(120,255); d.ellipse([x,y,x+r,y+r],fill=(a,a,255))
# sol listrado
sun=Image.new('RGBA',(W,H),(0,0,0,0)); sd=ImageDraw.Draw(sun)
R=250; cx,cy=W//2,HOR-40
for y in range(cy-R,cy+R):
    t=(y-(cy-R))/(2*R)
    col=(255,int(210-150*t),int(80+100*t),255)
    dx=int((R*R-(y-cy)**2)**0.5)
    # faixas horizontais na metade de baixo
    # faixas que engrossam em direção ao horizonte
    if t>0.28:
        u=(t-0.28)/0.27; periodo=26; corte=min(0.7,0.12+0.5*u)
        if ((y-cy)%periodo)/periodo < corte: continue
    sd.line([(cx-dx,y),(cx+dx,y)],fill=col)
glow=sun.filter(ImageFilter.GaussianBlur(40))
base=Image.alpha_composite(base.convert('RGBA'),glow)
base=Image.alpha_composite(base,sun)
# corta o sol abaixo do horizonte
arr=np.array(base); arr[HOR:,:,:3]=np.array(Image.fromarray(img.astype('uint8')))[HOR:]; base=Image.fromarray(arr)
# grade neon em perspectiva
grid=Image.new('RGBA',(W,H),(0,0,0,0)); g=ImageDraw.Draw(grid)
neon=(255,60,220,255)
for i in range(-40,41):
    xb=W/2+i*140; g.line([(W/2+i*6,HOR),(xb,H)],fill=neon,width=2)
N=16
for k in range(1,N+1):
    y=HOR+(H-HOR)*(k/N)**2.2
    g.line([(0,y),(W,y)],fill=neon,width=2)
g.line([(0,HOR),(W,HOR)],fill=(80,220,255,255),width=3)
gglow=grid.filter(ImageFilter.GaussianBlur(6))
base=Image.alpha_composite(base,gglow); base=Image.alpha_composite(base,grid)
base.convert('RGB').save(sys.argv[1],quality=88)
