"""Rebuild from Java 21 intermediary sources using your local Fabric/Minecraft dependencies."""
import argparse, pathlib, subprocess, zipfile, json, os
p=argparse.ArgumentParser()
p.add_argument('--minecraft',required=True)
p.add_argument('--fabric-api',required=True)
p.add_argument('--libraries',required=True)
p.add_argument('--javac',default='javac')
a=p.parse_args()
root=pathlib.Path(__file__).resolve().parent
build=root/'host-build'; build.mkdir(exist_ok=True)
deps=root/'host-deps'; deps.mkdir(exist_ok=True)
with zipfile.ZipFile(a.fabric_api) as api:
    for name in api.namelist():
        if name.endswith('.jar') and any(part in name for part in ['fabric-api-base-','fabric-screen-api-v1-','fabric-lifecycle-events-v1-']):
            (deps/pathlib.Path(name).name).write_bytes(api.read(name))
cp=[str(pathlib.Path(a.minecraft).resolve())]+[str(f.resolve()) for f in pathlib.Path(a.libraries).rglob('*.jar')]+[str(f) for f in deps.glob('*.jar')]
sources=[str(f) for f in (root/'src').rglob('*.java')]
args=['-proc:none','--release','21','-encoding','UTF-8','-cp',os.pathsep.join(cp),'-d',str(build)]+sources
argfile=root/'host-javac.args'
argfile.write_text('\n'.join(json.dumps(s) for s in args),encoding='utf-8')
subprocess.run([a.javac,'@'+str(argfile)],check=True)
manifest={'schemaVersion':1,'id':'krypton_addon_host','version':'1.0.0','name':'Krypton Addon Host','environment':'client','entrypoints':{'client':['krypton.addons.host.AddonHost']},'depends':{'fabricloader':'>=0.18.3','minecraft':'1.21.11','java':'>=21','fabric-api':'*','krloader':'*'}}
for name,select,metadata in [('krypton-addon-host-1.0.0.jar',lambda f:True,manifest),('krypton-addon-api-1.0.0.jar',lambda f:'api' in f.parts,None)]:
    with zipfile.ZipFile(root/name,'w',zipfile.ZIP_DEFLATED) as jar:
        jar.writestr('META-INF/MANIFEST.MF','Manifest-Version: 1.0\n\n')
        if metadata:jar.writestr('fabric.mod.json',json.dumps(metadata,indent=2))
        for f in build.rglob('*.class'):
            if select(f):jar.write(f,f.relative_to(build).as_posix())
    print(root/name)
