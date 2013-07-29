do local _={
		unknownglobalvars={

		}
		--[[table: 0x98b0d30]],
		content={
			localvars={
				[1]={
					item={
						type={
							def={
								shortdescription="",
								name="___",
								fields={
									fieldname={
										type={
											typename="number",
											tag="primitivetyperef"
										}
										--[[table: 0x989e160]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="fieldname",
										sourcerange={
											min=51,
											max=59
										}
										--[[table: 0x989e1d8]],
										occurrences={

										}
										--[[table: 0x989e1b0]],
										tag="item"
									}
									--[[table: 0x989e188]]
								}
								--[[table: 0x95a07e0]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x954ddd0]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x954dda8]],
							tag="inlinetyperef"
						}
						--[[table: 0x954ddf8]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x954dd20]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x98f7f20]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x9640e18]],
							[2]={
								sourcerange={
									min=22,
									max=30
								}
								--[[table: 0x96f5bd8]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x96f5b38]],
							[3]={
								sourcerange={
									min=41,
									max=49
								}
								--[[table: 0x96d5e50]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x97edf88]]
						}
						--[[table: 0x95d6358]],
						tag="item"
					}
					--[[table: 0x95d6330]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x95204e0]]
				}
				--[[table: 0x9520478]]
			}
			--[[table: 0x97e0838]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x97e0860]],
			content={
				[1]=nil --[[ref]],
				[2]=nil --[[ref]],
				[3]={
					sourcerange={
						min=41,
						max=59
					}
					--[[table: 0x98f9a88]],
					right="fieldname",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x94da738]]
			}
			--[[table: 0x97e0810]],
			tag="MBlock"
		}
		--[[table: 0x98b0d58]],
		tag="MInternalContent"
	}
	--[[table: 0x9459d20]];
	_.content.localvars[1].item.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[3].definition=_.content.localvars[1].item;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2]=_.content.localvars[1].item.occurrences[2];
	_.content.content[3].left=_.content.localvars[1].item.occurrences[3];
	return _;
end