from PIL import Image
import os

source_image = r"C:\Users\sanjeeb\.gemini\antigravity\brain\df1b14b9-fd2f-455d-b20c-92baa0f3f29d\notion_airdrop_icon_1782765944591.png"
base_dir = r"C:\Users\sanjeeb\Documents\Github\NotionDrop\app\src\main\res"

sizes = {
    "mdpi": {"launcher": 48, "foreground": 108},
    "hdpi": {"launcher": 72, "foreground": 162},
    "xhdpi": {"launcher": 96, "foreground": 216},
    "xxhdpi": {"launcher": 144, "foreground": 324},
    "xxxhdpi": {"launcher": 192, "foreground": 432}
}

img = Image.open(source_image).convert("RGBA")

for dpi, dims in sizes.items():
    folder = os.path.join(base_dir, f"mipmap-{dpi}")
    os.makedirs(folder, exist_ok=True)
    
    # foreground
    fg = img.resize((dims["foreground"], dims["foreground"]), Image.Resampling.LANCZOS)
    fg.save(os.path.join(folder, "ic_launcher_foreground.png"))
    
    # legacy launcher (and round)
    ln = img.resize((dims["launcher"], dims["launcher"]), Image.Resampling.LANCZOS)
    
    # create round version by masking
    import numpy as np
    mask = Image.new("L", ln.size, 0)
    from PIL import ImageDraw
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, ln.size[0], ln.size[1]), fill=255)
    
    round_ln = ln.copy()
    round_ln.putalpha(mask)
    
    ln.save(os.path.join(folder, "ic_launcher.png"))
    round_ln.save(os.path.join(folder, "ic_launcher_round.png"))

print("Icons updated successfully!")
