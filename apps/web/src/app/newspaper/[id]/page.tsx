import { NewspaperReader } from "@/components/public/NewspaperReader";

export default async function NewspaperReaderPage({ params }: { params: Promise<{ id: string }> }) { const { id } = await params; return <NewspaperReader id={id} />; }
