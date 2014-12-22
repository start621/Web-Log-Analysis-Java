package vforce.lei;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.util.*;

public class summary {
    public static class mapper extends MapReduceBase implements Mapper<LongWritable,Text,Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable>output, Reporter reporter)
        throws IOException{
            String line = value.toString();
            String newLine = line.replaceAll("\"\"","\"");

            JSONParser jsonParser = new JSONParser();
            ContainerFactory containerFactory = new ContainerFactory() {
                @Override
                public Map createObjectContainer() {
                    return new LinkedHashMap();
                }

                @Override
                public List creatArrayContainer() {
                    return new LinkedList();
                }
            };

            try{
                Map json = (Map)jsonParser.parse(newLine, containerFactory);
                Iterator it = json.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry me = (Map.Entry)it.next();
                    output.collect(new Text(me.getKey().toString()), one);
                }

            }catch(ParseException e){
                e.printStackTrace();
            }
        }
    }

    public static class reducer extends MapReduceBase implements Reducer<Text, IntWritable,Text, IntWritable> {
        public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable>output, Reporter reporter)
                throws IOException{

            int sum = 0;
            while(values.hasNext()){
                sum++;
            }
            output.collect(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) throws IOException{
        JobConf jobConf = new JobConf(summary.class);
        jobConf.setJobName("summary");

        jobConf.setOutputKeyClass(Text.class);
        jobConf.setOutputValueClass(IntWritable.class);

        jobConf.setMapperClass(mapper.class);
        jobConf.setReducerClass(reducer.class);
        jobConf.setCombinerClass(reducer.class);

        jobConf.setInputFormat(TextInputFormat.class);
        jobConf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(jobConf,new Path(args[1]));
        FileOutputFormat.setOutputPath(jobConf,new Path(args[2]));

        JobClient.runJob(jobConf);
    }
}