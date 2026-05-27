#!/usr/bin/env python3
"""
Generates Android launcher icons (ic_launcher.png & ic_launcher_round.png)
for all mipmap densities from a source image.
"""
import sys
from pathlib import Path
from PIL import Image

SRC = Path(sys.argv[1])          # source image path
RES = Path(sys.argv[2])          # app/src/main/res

# (folder, icon_size, canvas_size)
# Android adaptive icon: foreground canvas = 108dp, safe zone = 72dp
# For legacy icons we fill the full square.
DENSITIES = {
    "mipmap-mdpi":    48,
    "mipmap-hdpi":    72,
    "mipmap-xhdpi":   96,
    "mipmap-xxhdpi":  144,
    "mipmap-xxxhdpi": 192,
}

def make_icon(src_img: Image.Image, size: int) -> Image.Image:
    """Create a square PNG icon: black background + centered, scaled logo."""
    bg = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    
    # Scale logo to 75% of the icon size (leave some padding)
    logo_size = int(size * 0.75)
    logo = src_img.copy()
    logo.thumbnail((logo_size, logo_size), Image.LANCZOS)
    
    # Center on the black background
    offset = ((size - logo.width) // 2, (size - logo.height) // 2)
    if logo.mode == "RGBA":
        bg.paste(logo, offset, mask=logo)
    else:
        bg.paste(logo, offset)
    return bg

def make_round_icon(src_img: Image.Image, size: int) -> Image.Image:
    """Create a circular PNG icon: black circular background + centered logo."""
    from PIL import ImageDraw
    bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(bg)
    draw.ellipse([0, 0, size - 1, size - 1], fill=(0, 0, 0, 255))
    
    logo_size = int(size * 0.70)
    logo = src_img.copy()
    logo.thumbnail((logo_size, logo_size), Image.LANCZOS)
    
    offset = ((size - logo.width) // 2, (size - logo.height) // 2)
    if logo.mode == "RGBA":
        bg.paste(logo, offset, mask=logo)
    else:
        bg.paste(logo, offset)
    return bg

def main():
    src = Image.open(SRC).convert("RGBA")

    for folder, size in DENSITIES.items():
        out_dir = RES / folder
        out_dir.mkdir(parents=True, exist_ok=True)

        sq   = make_icon(src, size)
        rnd  = make_round_icon(src, size)

        sq.save(out_dir / "ic_launcher.png",       "PNG")
        rnd.save(out_dir / "ic_launcher_round.png", "PNG")
        print(f"  ✓ {folder}: {size}×{size}px")

    print("\nAll densities generated!")

if __name__ == "__main__":
    main()
